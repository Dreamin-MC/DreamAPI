package fr.dreamin.dreamapi.core.downloader;

import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntArrayTag;
import com.flowpowered.nbt.stream.NBTInputStream;
import fr.dreamin.dreamapi.api.DreamAPI;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for downloading files and clearing directories.
 * <p>
 * Used for resource downloads (e.g., NBT structures, JSON configs, etc.).
 *
 * @author Dreamin
 * @since 0.0.7
 *
 */
public final class NBTDownloader {

  private static final int CONNECT_TIMEOUT_MILLIS = 5_000, READ_TIMEOUT_MILLIS = 10_000, BUFFER_SIZE = 8_192;

  private static final String TAG_SIZE = "size", TAG_BLOCKS = "blocks", TAG_PALETTE = "palette";

  private static final Logger LOGGER = getLogger();

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Downloads a file from a given URL to the specified destination.
   * <p>
   * If the file already exists and {@code overwrite} is {@code false}, the existing file is returned.
   * <p>
   * After download, the file is optionally validated as a Minecraft NBT structure file
   * (gzip + NBT + basic structure tags).
   *
   * @param urlStr           the file URL
   * @param outputFile       the output file path (absolute or relative)
   * @param overwrite        whether to overwrite an existing file
   * @param validateNbt      whether to validate the downloaded file as a structure NBT
   * @return the downloaded {@link File}
   * @throws IOException if an I/O error occurs (connection, file write, or validation)
   */
  public static @NotNull File downloadFileFromURL(
    final @NotNull String urlStr,
    final @NotNull String outputFile,
    final boolean overwrite,
    final boolean validateNbt
  ) throws IOException {

    final var targetPath = Paths.get(outputFile).toAbsolutePath();
    final var targetFile = targetPath.toFile();

    if (targetFile.exists() && !overwrite) {
      LOGGER.info(() -> String.format("File already exists, skipping download: %s", targetFile));

      if (validateNbt)
        validateStructureFile(targetFile);
      return targetFile;
    }

    final var parent = targetPath.getParent();
    if (parent != null && Files.notExists(parent)) {
      Files.createDirectories(parent);
      LOGGER.fine(() -> String.format("Created parent directories for: %s", parent));
    }

    final var url = new URL(urlStr);
    final var connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
    connection.setReadTimeout(READ_TIMEOUT_MILLIS);
    connection.setInstanceFollowRedirects(true);

    final var status = connection.getResponseCode();
    if (status != HttpURLConnection.HTTP_OK) {
      final var errorMessage = readErrorMessage(connection);
      connection.disconnect();
      throw new IOException(String.format(
        "Failed to download file (%s). HTTP %d. %s",
        urlStr, status, errorMessage
      ));
    }

    final var tempFile = Files.createTempFile(
      parent != null ? parent : targetPath.getParent(),
      targetPath.getFileName().toString(),
      ".download"
    );

    try (
      final var inputStream = new BufferedInputStream(connection.getInputStream());
      final var outputStream = new BufferedOutputStream(Files.newOutputStream(
      tempFile, StandardOpenOption.TRUNCATE_EXISTING
    ))) {
      final var buffer = new byte[BUFFER_SIZE];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      try {
        Files.deleteIfExists(tempFile);
      } catch (IOException ex) {
        LOGGER.log(Level.WARNING, "Failed to delete temporary file: " + tempFile, ex);
      }
      throw e;
    } finally {
      connection.disconnect();
    }

    Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    LOGGER.info(() -> String.format("File downloaded successfully: %s", targetPath));

    if (validateNbt) {
      try {
        validateStructureFile(targetFile);
      } catch (IOException | IllegalArgumentException e) {
        LOGGER.log(Level.WARNING, "Downloaded file is not a valid NBT structure, deleting: " + targetFile.getAbsolutePath(), e);
        if (!targetFile.delete())
          LOGGER.warning("Failed to delete invalid structure file: " + targetFile.getAbsolutePath());
        throw new IOException("Downloaded file is not a valid structure NBT: " + urlStr, e);
      }
    }

    return targetFile;
  }

  /**
   * Deletes all files and sub-directories inside the specified folder.
   * The folder itself is kept.
   *
   * @param folderName the path of the folder
   * @throws IOException if an I/O error occurs while deleting
   */
  public static void clearFolder(final @NotNull String folderName) throws IOException {
    final var folder = Paths.get(folderName);
    if (Files.notExists(folder) || !Files.isDirectory(folder)) {
      LOGGER.warning(() -> String.format("Folder '%s' does not exist or is not a directory.", folderName));
      return;
    }

    try (var paths = Files.walk(folder)) {
      paths
        .filter(path -> !path.equals(folder))
        .sorted((p1, p2) -> p2.getNameCount() - p1.getNameCount())
        .forEach(path -> {
          try {
            Files.deleteIfExists(path);
            LOGGER.fine(() -> String.format("Deleted: %s", path));
          } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to delete: " + path, e);
          }
        });
    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Downloads logger from DreamAPI if available, or falls back to a standalone logger.
   *
   * @return a logger instance
   */
  private static Logger getLogger() {
    try {
      return DreamAPI.getAPI().getLogger();
    } catch (Exception ignored) {
      return Logger.getLogger("DreamAPI-NBTDownloader");
    }
  }

  /**
   * Tries to read a short error message from the HTTP error stream.
   *
   * @param connection the HTTP connection
   * @return an optional textual error message, never {@code null}
   */
  private static String readErrorMessage(final @NotNull HttpURLConnection connection) {
    try (final var errorStream = connection.getErrorStream()) {
      if (errorStream == null) return "";
      try (final var reader = new BufferedReader(new InputStreamReader(errorStream))) {
        final var sb = new StringBuilder();
        String line;
        int lines = 0;
        while ((line = reader.readLine()) != null && lines++ < 5) {
          sb.append(line).append('\n');
        }
        final var msg = sb.toString().trim();
        return msg.isEmpty() ? "" : "Server message: " + msg;
      }
    } catch (IOException e) {
      return "";
    }
  }

  // ###############################################################
  // ------------------- NBT VALIDATION HELPERS --------------------
  // ###############################################################

  /**
   * Validates that the given file is a valid, gzip-compressed Minecraft structure NBT file.
   * <p>
   * This method checks:
   * <ul>
   *   <li>The file can be opened as a GZIP stream.</li>
   *   <li>The NBT root tag is a {@link CompoundTag}.</li>
   *   <li>The root contains basic structure tags such as {@code size}, {@code blocks}, and {@code palette}.</li>
   * </ul>
   *
   * @param file the file to validate
   * @throws IOException              if the file cannot be read or parsed
   * @throws IllegalArgumentException if the logical structure does not match a Minecraft structure
   */
  private static void validateStructureFile(final @NotNull File file) throws IOException {

    if (!file.exists() || !file.isFile()) {
      throw new IOException("Structure file does not exist: " + file.getAbsolutePath());
    }

    // Validate GZIP and read NBT root
    final CompoundTag root = readRootNbt(file);

    // Basic logical validation of expected structure tags
    validateStructureRoot(root);
  }

  /**
   * Reads the NBT root from a gzip-compressed NBT file.
   *
   * @param file the NBT file
   * @return the root compound tag
   * @throws IOException if reading or parsing fails
   */
  private static @NotNull CompoundTag readRootNbt(final @NotNull File file) throws IOException {
    try (var fis = new FileInputStream(file);
         var bis = new BufferedInputStream(fis);
         var gis = new GZIPInputStream(bis);
         var nbtIn = new NBTInputStream(gis, false)) { // false = big-endian (standard NBT)
      var tag = nbtIn.readTag();
      if (!(tag instanceof CompoundTag compound)) {
        throw new IOException("NBT root is not a CompoundTag");
      }
      return compound;
    }
  }

  /**
   * Performs basic logical validation on a Minecraft structure NBT root compound.
   * <p>
   * This checks for the presence of the {@code size}, {@code blocks}, and {@code palette} tags and
   * verifies the {@code size} tag contains reasonable dimensions.
   *
   * @param root the root compound tag
   * @throws IllegalArgumentException if the structure is logically invalid
   */
  private static void validateStructureRoot(final @NotNull CompoundTag root) {
    final var size = getInts(root);

    final var x = size[0];
    final var y = size[1];
    final var z = size[2];

    if (x <= 0 || y <= 0 || z <= 0) {
      throw new IllegalArgumentException("Invalid structure dimensions: non-positive size.");
    }

    final var maxX = 64;
    final var maxY = 255;
    final var maxZ = 64;
    if (x > maxX || y > maxY || z > maxZ) {
      throw new IllegalArgumentException(String.format(
        "Structure size too large (%d,%d,%d) > (%d,%d,%d).",
        x, y, z, maxX, maxY, maxZ
      ));
    }
  }

  private static int @NonNull [] getInts(final @NotNull CompoundTag root) {
    var value = root.getValue();

    if (!value.containsKey(TAG_SIZE)
      || !value.containsKey(TAG_BLOCKS)
      || !value.containsKey(TAG_PALETTE)) {
      throw new IllegalArgumentException("Invalid structure NBT: required tags missing (size/blocks/palette).");
    }

    final var sizeTag = value.get(TAG_SIZE);
    if (!(sizeTag instanceof IntArrayTag sizeArray)) {
      throw new IllegalArgumentException("Invalid 'size' tag: expected IntArrayTag.");
    }

    final var size = sizeArray.getValue();
    if (size.length != 3) {
      throw new IllegalArgumentException("Invalid 'size' tag: expected array of length 3.");
    }
    return size;
  }
}