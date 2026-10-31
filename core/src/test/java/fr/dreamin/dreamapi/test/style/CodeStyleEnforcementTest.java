package fr.dreamin.dreamapi.test.style;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated Code Style Enforcement Test Suite for DreamAPI (Milestone 1).
 * Permanently locks in:
 * 1. Rule 1: 'final var' priority on local variable declarations.
 * 2. Rule 2: Omission of redundant braces on single-statement control blocks.
 * 3. Rule 3: Top-level imports and elimination of inline fully-qualified class names.
 * 4. Requirement R0: Complete elimination of 'plugin-base' across the 22 active subprojects.
 */
@DisplayName("Code Style & Architectural Governance Enforcement (Milestone 1)")
public class CodeStyleEnforcementTest {

  private static final Path PROJECT_ROOT = findProjectRoot();

  private static final List<String> ACTIVE_22_SUBPROJECTS = List.of(
    "core", "nms", "item", "gui", "recipe", "cuboid", "particle",
    "hologram", "glowing", "worldborder", "world", "navigate", "lang",
    "dialog", "skin", "database", "http", "command", "animation",
    "hooks", "all", "example-plugin"
  );

  private static final Pattern FQCN_PATTERN = Pattern.compile(
    "\\b(org\\.bukkit|java\\.(?:util|io|nio|time|concurrent|lang)|net\\.kyori|fr\\.dreamin|com\\.google)\\.[a-zA-Z0-9_.]*?[A-Z][a-zA-Z0-9_]*\\b"
  );

  private static final Pattern BRACELESS_CANDIDATE_PATTERN = Pattern.compile(
    "\\b(if\\s*\\([^)]+\\)|else|for\\s*\\([^)]+\\)|while\\s*\\([^)]+\\))\\s*\\{\\s*([^{};]+;)\\s*\\}"
  );

  private static final Pattern TYPE_DECL_PATTERN = Pattern.compile(
    "\\b(class|interface|enum|record)\\b"
  );

  private static final Pattern ANONYMOUS_CLASS_PATTERN = Pattern.compile(
    "new\\s+[A-Za-z0-9_<>.]*\\s*\\([^)]*\\)\\s*$"
  );

  private static final Pattern FIELD_MODIFIERS_PATTERN = Pattern.compile(
    "^(?:private|protected|public|transient|volatile)\\b"
  );

  private static final Pattern ENHANCED_FOR_PATTERN = Pattern.compile(
    "^for\\s*\\(\\s*(?:@\\w+\\s+)?(?:final\\s+)?([A-Za-z0-9_<>?, .\\[\\]]+)\\s+([A-Za-z0-9_]+)\\s*:"
  );

  private static final Pattern LOCAL_DECL_PATTERN = Pattern.compile(
    "^(?:@\\w+(?:\\([^)]*\\))?\\s+)*(final\\s+)?([A-Za-z_][A-Za-z0-9_.]*(?:<[A-Za-z0-9_<>?, .\\[\\]]+>)?(?:\\[\\])*)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+);$"
  );

  private static final Set<String> NON_TYPE_KEYWORDS = Set.of(
    "return", "throw", "if", "while", "for", "else", "switch", "case", "default",
    "break", "continue", "new", "this", "super", "assert", "yield", "import", "package"
  );

  // =========================================================================
  // TEST 1: Project Root, Active Subprojects & File Discovery (Requirement R0)
  // =========================================================================

  @Test
  @DisplayName("TC-STYLE-01: Verify 22 active subprojects & plugin-base elimination (R0)")
  void testActiveSubprojectsAndDiscovery() throws IOException {
    assertNotNull(PROJECT_ROOT, "Project root must be resolvable");
    assertTrue(Files.exists(PROJECT_ROOT.resolve("settings.gradle")), "settings.gradle must exist at project root");
    assertTrue(Files.exists(PROJECT_ROOT.resolve("build.gradle")), "build.gradle must exist at project root");

    // Requirement R0: plugin-base module must be eliminated
    final var pluginBaseDir = PROJECT_ROOT.resolve("plugin-base");
    if (Files.exists(pluginBaseDir)) {
      try (final var stream = Files.walk(pluginBaseDir)) {
        final var javaFiles = stream.filter(p -> p.getFileName().toString().endsWith(".java")).count();
        assertEquals(0, javaFiles, "Requirement R0: plugin-base directory must contain 0 Java files");
      }
    }

    // Verify all 22 active subprojects exist
    for (final var subproject : ACTIVE_22_SUBPROJECTS) {
      final var dir = PROJECT_ROOT.resolve(subproject);
      assertTrue(Files.isDirectory(dir), "Active subproject directory must exist: " + subproject);
    }

    // Discover all Java files across active subprojects
    final var allFiles = findAllProjectJavaFiles();
    assertTrue(allFiles.size() >= 360, "Must discover all project Java files (found: " + allFiles.size() + ")");
  }

  // =========================================================================
  // TEST 2: Rule 1 — 'final var' Priority
  // =========================================================================

  @Test
  @DisplayName("TC-STYLE-02: Rule 1 — Local variables use 'final var' where valid")
  void testRule1FinalVarPriority() throws IOException {
    final var allFiles = findAllProjectJavaFiles();
    final var violations = new ArrayList<String>();

    for (final var file : allFiles) {
      final var relPath = normalizePath(PROJECT_ROOT.relativize(file));
      final var fileViolations = scanRule1Violations(file, relPath);
      violations.addAll(fileViolations);
    }

    assertTrue(violations.isEmpty(),
      "Rule 1 ('final var' priority) failed with " + violations.size() + " violations:\n"
        + String.join("\n", violations.subList(0, Math.min(violations.size(), 30)))
        + (violations.size() > 30 ? "\n... and " + (violations.size() - 30) + " more" : "")
    );
  }

  // =========================================================================
  // TEST 3: Rule 2 — Braceless Single-Statement Blocks
  // =========================================================================

  @Test
  @DisplayName("TC-STYLE-03: Rule 2 — Omit redundant braces on single-statement blocks")
  void testRule2SingleStatementBracelessBlocks() throws IOException {
    final var allFiles = findAllProjectJavaFiles();
    final var violations = new ArrayList<String>();

    for (final var file : allFiles) {
      final var relPath = normalizePath(PROJECT_ROOT.relativize(file));
      final var fileViolations = scanRule2Violations(file, relPath);
      violations.addAll(fileViolations);
    }

    assertEquals(0, violations.size(),
      "Rule 2 (redundant braces) failed with " + violations.size() + " violations:\n"
        + String.join("\n", violations.subList(0, Math.min(violations.size(), 30)))
        + (violations.size() > 30 ? "\n... and " + (violations.size() - 30) + " more" : "")
    );
  }

  // =========================================================================
  // TEST 4: Rule 3 — Top-Level Imports & Zero Avoidable Inline FQCNs
  // =========================================================================

  @Test
  @DisplayName("TC-STYLE-04: Rule 3 — Top-level imports (zero avoidable inline FQCNs)")
  void testRule3TopLevelImportsAndNoInlineFqcn() throws IOException {
    final var allFiles = findAllProjectJavaFiles();
    final var violations = new ArrayList<String>();

    for (final var file : allFiles) {
      final var relPath = normalizePath(PROJECT_ROOT.relativize(file));
      final var fileViolations = scanRule3Violations(file, relPath);
      violations.addAll(fileViolations);
    }

    assertTrue(violations.isEmpty(),
      "Rule 3 (inline FQCN) failed with " + violations.size() + " violations:\n"
        + String.join("\n", violations)
    );
  }

  // =========================================================================
  // SCANNER & HELPER IMPLEMENTATIONS
  // =========================================================================

  private static Path findProjectRoot() {
    var current = Path.of("").toAbsolutePath();
    while (current != null) {
      if (Files.exists(current.resolve("settings.gradle")) && Files.exists(current.resolve("build.gradle"))) {
        return current;
      }
      current = current.getParent();
    }
    return Path.of("O:/Dreamin/projects/java/DreamAPI").toAbsolutePath();
  }

  public static List<Path> findAllProjectJavaFiles() throws IOException {
    try (final var stream = Files.walk(PROJECT_ROOT)) {
      return stream
        .filter(Files::isRegularFile)
        .filter(p -> p.getFileName().toString().endsWith(".java"))
        .filter(p -> {
          final var norm = normalizePath(p);
          return !norm.contains("/build/")
              && !norm.contains("/.gradle/")
              && !norm.contains("/.tmp/")
              && !norm.contains("/.git/")
              && !norm.contains("/.idea/")
              && !norm.contains("/.agents/");
        })
        .sorted()
        .toList();
    }
  }

  private static String normalizePath(Path path) {
    return path.toString().replace('\\', '/');
  }

  private static List<String> scanRule1Violations(Path file, String relPath) throws IOException {
    final var content = Files.readString(file, StandardCharsets.UTF_8);
    final var lines = content.split("\\R");
    final var violations = new ArrayList<String>();

    final var scopeStack = new ArrayList<String>();
    final var headerBuilder = new StringBuilder();
    var inBlockComment = false;

    for (var lineIdx = 0; lineIdx < lines.length; lineIdx++) {
      final var rawLine = lines[lineIdx];
      final var codeLine = new StringBuilder();
      var inString = false;
      var inChar = false;

      for (var cIdx = 0; cIdx < rawLine.length(); cIdx++) {
        final var c = rawLine.charAt(cIdx);
        final var next = cIdx + 1 < rawLine.length() ? rawLine.charAt(cIdx + 1) : '\0';

        if (inBlockComment) {
          if (c == '*' && next == '/') {
            inBlockComment = false;
            cIdx++;
          }
          continue;
        }
        if (inString) {
          if (c == '\\') { cIdx++; continue; }
          if (c == '"') inString = false;
          continue;
        }
        if (inChar) {
          if (c == '\\') { cIdx++; continue; }
          if (c == '\'') inChar = false;
          continue;
        }

        if (c == '/' && next == '/') break;
        if (c == '/' && next == '*') { inBlockComment = true; cIdx++; continue; }
        if (c == '"') { inString = true; continue; }
        if (c == '\'') { inChar = true; continue; }

        if (c == '{') {
          final var header = headerBuilder.toString().trim();
          headerBuilder.setLength(0);
          if (TYPE_DECL_PATTERN.matcher(header).find() || ANONYMOUS_CLASS_PATTERN.matcher(header).find())
            scopeStack.add("CLASS");
          else if (scopeStack.isEmpty() || "CLASS".equals(scopeStack.get(scopeStack.size() - 1)))
            scopeStack.add("METHOD");
          else
            scopeStack.add("BLOCK");
          continue;
        } else if (c == '}') {
          headerBuilder.setLength(0);
          if (!scopeStack.isEmpty())
            scopeStack.remove(scopeStack.size() - 1);
          continue;
        }

        headerBuilder.append(c);
        codeLine.append(c);
      }

      final var inMethod = !scopeStack.isEmpty()
        && ("METHOD".equals(scopeStack.get(scopeStack.size() - 1)) || "BLOCK".equals(scopeStack.get(scopeStack.size() - 1)));
      if (!inMethod) continue;

      final var trimmed = codeLine.toString().trim();
      if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("*")) continue;
      if (FIELD_MODIFIERS_PATTERN.matcher(trimmed).find()) continue;

      // Enhanced for-loop check
      final var forMatcher = ENHANCED_FOR_PATTERN.matcher(trimmed);
      if (forMatcher.find()) {
        final var isFinalVar = trimmed.matches("^for\\s*\\(\\s*(?:final\\s+var|final\\s+@\\w+\\s+var|@\\w+\\s+final\\s+var)\\b.*");
        if (!isFinalVar)
          violations.add("[" + relPath + ":" + (lineIdx + 1) + "] Enhanced for-loop must use 'final var': " + rawLine.trim());
        continue;
      }

      // Local variable declaration check
      final var declMatcher = LOCAL_DECL_PATTERN.matcher(trimmed);
      if (declMatcher.matches()) {
        final var hasFinal = declMatcher.group(1) != null;
        final var typeName = declMatcher.group(2).trim();
        final var varName = declMatcher.group(3).trim();
        final var rhs = declMatcher.group(4).trim();

        if (NON_TYPE_KEYWORDS.contains(typeName) || typeName.startsWith("this.")) continue;

        if ("var".equals(typeName)) {
          if (!hasFinal && !isMutatedLater(varName, lines, lineIdx + 1))
            violations.add("[" + relPath + ":" + (lineIdx + 1) + "] Variable '" + varName + "' is not reassigned and must be 'final var': " + rawLine.trim());
        } else if (!"final var".equals(typeName) && !(hasFinal && "var".equals(typeName))) {
          // Explicit type: check valid Java type inference exemptions
          final var isExempt = "null".equals(rhs)
            || rhs.startsWith("{")
            || (rhs.contains("->") && !rhs.startsWith("(" + typeName + ")"));
          if (!isExempt)
            violations.add("[" + relPath + ":" + (lineIdx + 1) + "] Local variable must use 'final var': " + rawLine.trim());
        }
      }
    }
    return violations;
  }

  private static boolean isMutatedLater(String varName, String[] lines, int startLine) {
    final var assignPattern = Pattern.compile("\\b" + Pattern.quote(varName) + "\\s*(\\+\\+|--|\\+=|-=|\\*=|/=|=(?!=))");
    for (var i = startLine; i < Math.min(lines.length, startLine + 300); i++) {
      final var l = lines[i].trim();
      if (l.startsWith("//") || l.startsWith("*")) continue;
      if (assignPattern.matcher(l).find()) return true;
    }
    return false;
  }

  private static List<String> scanRule2Violations(Path file, String relPath) throws IOException {
    final var rawContent = Files.readString(file, StandardCharsets.UTF_8);
    // Replace comments and strings with spaces to keep exact offsets
    final var cleanContent = stripCommentsAndStrings(rawContent);
    final var violations = new ArrayList<String>();
    final var matcher = BRACELESS_CANDIDATE_PATTERN.matcher(cleanContent);

    while (matcher.find()) {
      final var header = matcher.group(1).trim();
      final var stmt = matcher.group(2).trim();

      // Check declaration restriction: Java requires braces if single statement is a variable declaration
      if (isDeclarationStatement(stmt)) continue;

      // Check dangling-else restriction
      if (header.startsWith("if") && stmt.startsWith("if ") && !stmt.contains("else")) {
        final var postMatch = cleanContent.substring(matcher.end()).trim();
        if (postMatch.startsWith("else")) continue;
      }

      // Calculate line number
      final var lineNum = 1 + (int) cleanContent.substring(0, matcher.start()).chars().filter(ch -> ch == '\n').count();
      violations.add("[" + relPath + ":" + lineNum + "] Redundant braces around single statement: " + header + " { " + stmt + " }");
    }
    return violations;
  }

  private static boolean isDeclarationStatement(String stmt) {
    return stmt.startsWith("final var ") || stmt.startsWith("var ")
      || stmt.matches("^(?:final\\s+)?[A-Z][a-zA-Z0-9_<>?, .\\[\\]]+\\s+[a-zA-Z0-9_]+\\s*=.*");
  }

  private static List<String> scanRule3Violations(Path file, String relPath) throws IOException {
    final var content = Files.readString(file, StandardCharsets.UTF_8);
    final var lines = content.split("\\R");
    final var violations = new ArrayList<String>();

    for (var i = 0; i < lines.length; i++) {
      final var rawLine = lines[i];
      final var trimmed = rawLine.trim();

      if (trimmed.startsWith("package ") || trimmed.startsWith("import ")
          || trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
        continue;
      }

      final var codeOnly = rawLine.replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"\"");
      final var matcher = FQCN_PATTERN.matcher(codeOnly);

      while (matcher.find()) {
        final var match = matcher.group();
        if (isWhitelistedCollision(relPath, match)) continue;

        violations.add("[" + relPath + ":" + (i + 1) + "] Avoidable inline FQCN '" + match + "' -> move to top-level import: " + trimmed);
      }
    }
    return violations;
  }

  private static boolean isWhitelistedCollision(String relPath, String fqcn) {
    // Collision 1: Test subclass sharing identical simple name with parent class
    if (relPath.endsWith("RuntimeDecouplingTest.java")
        && fqcn.equals("fr.dreamin.dreamapi.core.decoupling.RuntimeDecouplingTest")) {
      return true;
    }
    // Collision 2: awt Color vs org.bukkit.Color in ColorCycleAnimation
    if (relPath.endsWith("ColorCycleAnimation.java") && fqcn.equals("org.bukkit.Color")) {
      return true;
    }
    // Collision 3: particle backward-compatible delegate forwarding to core InterpolationType
    if (relPath.endsWith("InterpolationType.java")
        && fqcn.equals("fr.dreamin.dreamapi.api.util.math.InterpolationType")) {
      return true;
    }
    // Collision 4: particle backward-compatible delegate forwarding to core Interpolation
    if (relPath.endsWith("Interpolation.java")
        && (fqcn.equals("fr.dreamin.dreamapi.api.util.math.Interpolation")
            || fqcn.equals("fr.dreamin.dreamapi.api.util.math.InterpolationType"))) {
      return true;
    }
    return false;
  }

  private static String stripCommentsAndStrings(String code) {
    final var sb = new StringBuilder(code.length());
    var inBlock = false;
    var inLine = false;
    var inString = false;
    var inChar = false;

    for (var i = 0; i < code.length(); i++) {
      final var c = code.charAt(i);
      final var next = i + 1 < code.length() ? code.charAt(i + 1) : '\0';

      if (inLine) {
        if (c == '\n') { inLine = false; sb.append('\n'); }
        else sb.append(' ');
        continue;
      }
      if (inBlock) {
        if (c == '*' && next == '/') { inBlock = false; i++; sb.append("  "); }
        else if (c == '\n') sb.append('\n');
        else sb.append(' ');
        continue;
      }
      if (inString) {
        if (c == '\\') { i++; sb.append("  "); continue; }
        if (c == '"') inString = false;
        sb.append(' ');
        continue;
      }
      if (inChar) {
        if (c == '\\') { i++; sb.append("  "); continue; }
        if (c == '\'') inChar = false;
        sb.append(' ');
        continue;
      }

      if (c == '/' && next == '/') { inLine = true; i++; sb.append("  "); continue; }
      if (c == '/' && next == '*') { inBlock = true; i++; sb.append("  "); continue; }
      if (c == '"') { inString = true; sb.append(' '); continue; }
      if (c == '\'') { inChar = true; sb.append(' '); continue; }

      sb.append(c);
    }
    return sb.toString();
  }
}
