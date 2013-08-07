package org.echosoft.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Tool for working with preferences.
 * The main purposes are importing preferences (with overwriting old values and withouth of them) and exporting.
 * Preferences represents as properties-like structure and looks like as
 * a.b=c
 * Where a - preferences key, b - preferences property, c = property value.
 * If name of preferences key or name of preferences property should contains char '.' then it should be masked with
 * '\' character. Double '\' translated as single '\'.
 *
 * @author Anton Sharapov
 */
public class PreferencesLoader {

    private static final String IO_ENCODING = "UTF-8";
    private static final Map<String, Preferences> scopes;
    static {
        scopes = new HashMap<String, Preferences>();
        scopes.put("user", Preferences.userRoot());
        scopes.put("system", Preferences.systemRoot());
    }


    /**
     * <p>Import data from stream to preferences.</p>
     * Example data represented below:
     * <pre>
     *  p1=d
     *  A.B.p2=x
     *  A.B.p3=y
     *  A.C.p4=z
     *  A.p5=${A.B.p3}
     *  user:D.p6=xxx
     *  system:D.p7=xxx
     * </pre>
     *
     * @param in             an input stream with the preferences data in the properties form.
     * @param overwritePrefs preferences should be overwrited if this param equals <code>true</code>
     * @throws IOException in case of any input/output errors.
     */
    public static void importPreferences(final Reader in, final boolean overwritePrefs) throws IOException {
        final StringBuilder buf = new StringBuilder();
        int c;
        while ( (c = in.read()) >= 0) {
            if (c != '\n') {
                buf.append((char)c);
            } else {
                importNode(buf.toString(), overwritePrefs);
                buf.setLength(0);
            }
        }
    }


    /**
     * Recursively export preferences to given writer.
     *
     * @param node root preference node that should be exported.
     * @param out  writer
     * @throws IOException           in case of any io errors.
     * @throws BackingStoreException in case of any errors with preferences backing store.
     */
    public static void exportPreferences(final Preferences node, final Appendable out) throws IOException, BackingStoreException {
        String path = node.absolutePath();
        if (path.indexOf('/') == 0)
            path = path.substring(1);
        path = encode(path, true);

        final String keys[] = node.keys();
        for (int i = 0; i < keys.length; i++) {
            if (!node.isUserNode())
                out.append("system:");
            out.append(path);
            out.append('.');
            out.append(encode(keys[i], false));
            out.append('=');
            out.append(node.get(keys[i], ""));
            out.append('\n');
        }

        final String childrenNames[] = node.childrenNames();
        for (int i = 0; i < childrenNames.length; i++) {
            final Preferences child = node.node(childrenNames[i]);
            exportPreferences(child, out);
        }
    }

    /**
     * Recuresively exports preferences to given map.
     *
     * @param node       root preference that should be exported.
     * @param result     map that will contains result of the export.
     * @param skipPrefix if true the path of the root node will be skipped.
     * @throws BackingStoreException in case of  any errors with preferences backing store.
     */
    public static void exportPreferences(final Preferences node, final Map<String, String> result, final boolean skipPrefix) throws BackingStoreException {
        final int skippedPrefixLength;
        if (skipPrefix) {
            final String path = encode(node.absolutePath(), true);
            skippedPrefixLength = path.length();
        } else {
            skippedPrefixLength = 0;
        }
        exportPreferences(node, result, skippedPrefixLength);
    }

    private static void exportPreferences(final Preferences node, final Map<String, String> result, final int skippedPrefixLength) throws BackingStoreException {
        String path = encode(node.absolutePath(), true);
        if (skippedPrefixLength > 0) {
            path = path.substring(skippedPrefixLength);
        }
        if (path.indexOf('.') == 0)
            path = path.substring(1);

        String template = (node.isUserNode() || skippedPrefixLength > 0 ? "" : "system:") + path;
        if (path.length() > 0)
            template += '.';

        final String keys[] = node.keys();
        for (int i = 0; i < keys.length; i++) {
            final String key = template + encode(keys[i], false);
            final String value = node.get(keys[i], "");
            result.put(key, value);
        }

        final String childrenNames[] = node.childrenNames();
        for (int i = 0; i < childrenNames.length; i++) {
            final Preferences child = node.node(childrenNames[i]);
            exportPreferences(child, result, skippedPrefixLength);
        }
    }


    /**
     * Retrieves preference node. Examples of expressions are:
     * <li> <b>a.b.c</b> - lookup preference node <b>a/b/c</b> in the user scope.
     * <li> <b>system:a.b.c</b> - lookup preference node <b>a/b/c</b> in the system scope.
     * <li> <b>user:a</b> - lookup node <b>a</b> from the user preference.
     * <li> <b>aaa\.bbb.ccc</b> - lookup preference node <b>aaa.bbb/ccc</b> in the user scope.
     *
     * @param expr         specifies preference node. Mandatory argument.
     * @param ensureExists if specified preferences node wasn't exists yet it will be created.
     * @return preference node or <code>null</code>.
     * @throws BackingStoreException in case of any errors in the preferences backing store.
     */
    public static Preferences getPreferenceNode(String expr, final boolean ensureExists) throws BackingStoreException {
        if (expr == null || (expr = expr.trim()).isEmpty())
            return null;

        final Preferences root;
        final int s1 = expr.indexOf(':');
        if (s1 > 0) {
            final String scope = expr.substring(0, s1);
            root = scopes.get(scope);
            if (root == null)
                throw new IllegalArgumentException("Unknown scope [" + scope + "] at the line [" + expr + "]");
            expr = expr.substring(s1 + 1);
        } else {
            root = Preferences.userRoot();
        }

        final String path = decode(expr, true);
        return ensureExists || root.nodeExists(path) ? root.node(path) : null;
    }

    /**
     * Retrieves preference value. Examples of expressions are:
     * <li> <b>a.b.c</b> - get attribute <b>c</b> from the preference <b>a/b</b> in the user scope.
     * <li> <b>system:a.b.c</b> - get attribute <b>c</b> from the preference <b>a/b</b> in the system scope.
     * <li> <b>user:a</b> - get attribute <b>a</b> from the root user preference.
     * <li> <b>aaa\.bbb.ccc</b> - get attribute <b>ccc</b> from the preference <b>aaa.bbb</b> in the user scope.
     *
     * @param expr specifies preference and preference key. Mandatory argument.
     * @return preference value or <code>null</code>.
     */
    public static String getPreferenceValue(String expr) {
        if (expr == null || (expr = expr.trim()).isEmpty())
            return null;

        final Preferences root;
        final int s1 = expr.indexOf(':');
        if (s1 > 0) {
            final String scope = expr.substring(0, s1);
            root = scopes.get(scope);
            if (root == null)
                throw new IllegalArgumentException("Unknown scope [" + scope + "] at the line [" + expr + "]");
            expr = expr.substring(s1 + 1);
        } else {
            root = Preferences.userRoot();
        }

        final int s2 = findKeyPathSeparator(expr);
        final String path, key;
        if (s2 >= 0) {
            path = decode(expr.substring(0, s2), true).trim();
            key = decode(expr.substring(s2 + 1), false).trim();
        } else {
            path = "";
            key = decode(expr, false);
        }

        final Preferences pref = root.node(path);
        return pref.get(key, null);
    }


    private static Preferences importNode(String line, final boolean overwritePrefs) {
        line = line.trim();
        if (line.length() == 0 || line.indexOf('#') == 0)
            return null;

        final int s3 = line.indexOf('=');
        if (s3 <= 0)
            throw new IllegalArgumentException("Unable to parse line [" + line + "]");

        final Preferences root;
        final String ref;
        final int s1 = line.indexOf(':');
        if (s1 > 0 && s1 < s3) {
            final String scope = line.substring(0, s1);
            root = scopes.get(scope);
            if (root == null)
                throw new IllegalArgumentException("Unknown scope [" + scope + "] at the line [" + line + "]");
            ref = line.substring(s1 + 1, s3);
        } else {
            root = Preferences.userRoot();
            ref = line.substring(0, s3);
        }

        final int s2 = findKeyPathSeparator(ref);
        final String path, key;
        if (s2 >= 0) {
            path = decode(ref.substring(0, s2), true).trim();
            key = decode(ref.substring(s2 + 1), false).trim();
        } else {
            path = "";
            key = decode(ref, false);
        }

        String value = line.substring(s3 + 1).trim();
        value = resolveExpression(value);
        final Preferences node = root.node(path);

        if (overwritePrefs || node.get(key, null) == null) {
            node.put(key, value);
            return node;
        }
        return null;
    }


    private static int findKeyPathSeparator(final String line) {
        final int endPos = line.length() - 1;
        for (int i = endPos; i >= 0; i--) {
            final char c = line.charAt(i);
            if (c == '.') {
                if (i > 0 && line.charAt(i - 1) == '\\')
                    continue;
                return i;
            }
        }
        return -1;
    }

    private static String decode(final String text, final boolean decodePath) {
        if (text == null)
            return "";
        final int length = text.length();
        final StringBuilder out = new StringBuilder(length);
        boolean escaped = false;
        for (int i = 0; i < length; i++) {
            final char c = text.charAt(i);
            if (c == '/')
                throw new IllegalArgumentException("Incorrect symbol as pos. " + i);
            if (escaped) {
                if (c == '\\') {
                    out.append('\\');
                } else
                if (c == '.') {
                    out.append('.');
                }
                escaped = false;
            } else
            if (c == '\\') {
                escaped = true;
            } else
            if (c == '.' && decodePath) {
                out.append('/');
            } else
                out.append(c);
        }
        return out.toString();
    }

    private static String encode(final String text, final boolean encodePath) {
        if (text == null)
            return "";
        final int length = text.length();
        final StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = text.charAt(i);
            switch (c) {
                case '\\': {
                    out.append("\\\\");
                    break;
                }
                case '.': {
                    out.append("\\.");
                    break;
                }
                case '/': {
                    out.append(encodePath ? '.' : '/');
                    break;
                }
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    private static String resolveExpression(final String text) {
        if (text == null)
            return text;

        final int endPos = text.length() - 1;
        int i = text.indexOf("${", 0);
        int j = (i >= 0) ? text.indexOf('}', i) : -2;
        if (i < 0 || j < 0) {
            return text;
        } else
        if (i == 0 && j == endPos) {  // most frequent case when text contains only one expression ...
            return getPreferenceValue(text.substring(i + 2, j));
        } else {                  // common and more expensive algorithm ...
            final StringBuilder result = new StringBuilder();
            int startPos = 0;
            while (j > i) {
                if (i > startPos)
                    result.append(text.substring(startPos, i));
                final String value = getPreferenceValue(text.substring(i + 2, j));
                if (value != null)
                    result.append(value);
                startPos = j + 1;
                i = text.indexOf("${", startPos);
                j = (i >= 0) ? text.indexOf('}', i) : -2;
            }
            if (startPos <= endPos)
                result.append(text.substring(startPos));
            return result.toString();
        }
    }


    public static void main(final String args[]) throws Exception {
        if (args.length < 1) {
            System.out.println("Java Preferences Tool");
            System.out.println("usage:");
            System.out.println("  ru.topsbi.common.util.PreferencesLoader import [<options>] <preferences file>");
            System.out.println("    where options are: ");
            System.out.println("      -overwrite      - overwrites existing preferences values");
            System.out.println("      -append         - does not overwrites existing preferences values and only appends new data (used by default)");
            System.out.println("    if source preferences was not specified then will be used system input stream");
            System.out.println("    By default all preferences will be imported into user scope");
            System.out.println("  or");
            System.out.println("  ru.topsbi.common.util.PreferencesLoader export [<options>] <preferences file>");
            System.out.println("    where options are: ");
            System.out.println("      -prefix=<path>  - references to root preference node for export");
            System.out.println("    If target preferences file was not specified then will be used system output stream");
            System.out.println("  or");
            System.out.println("  ru.topsbi.common.util.PreferencesLoader clean [<options>]");
            System.out.println("    where options are: ");
            System.out.println("      -prefix=<path>  - reference to root preference node for cleaning");
            System.exit(1);
        }

        if ("import".equals(args[0])) {
            boolean overwrite = false;
            File file = null;
            int i = 1;
            for (; i < args.length && args[i].startsWith("-"); i++) { // parse options...
                if ("-overwrite".equals(args[i])) {
                    overwrite = true;
                } else
                if ("append".equals(args[i])) {
                    overwrite = false;
                } else {
                    System.err.println("Unknown option: " + args[i]);
                    System.exit(1);
                }
            }
            if (i < args.length)
                file = new File(args[i]);
            processImport(overwrite, file);
        } else
        if ("export".equals(args[0])) {
            String prefix = "";
            File file = null;
            int i = 1;
            for (; i < args.length && args[i].startsWith("-"); i++) { // parse options...
                if ("-user".equals(args[i])) {
                    System.err.println("Warning! '-user' option deprecated now. You should use '-prefix=user:a.b.c'");
                } else
                if ("-system".equals(args[i])) {
                    System.err.println("Warning! '-system' option deprecated now. You should use '-prefix=system:a.b.c'");
                } else
                if ("-both".equals(args[i])) {
                    System.err.println("Warning! '-both' option deprecated now with no direct replacement.");
                } else
                if (args[i].startsWith("-prefix=")) {
                    prefix = args[i].substring(8);
                } else {
                    System.err.println("Unknown option: " + args[i]);
                    System.exit(1);
                }
            }
            if (i < args.length)
                file = new File(args[i]);
            processExport(prefix, file);
        } else
        if ("clean".equals(args[0])) {
            String prefix = "";
            int i = 1;
            for (; i < args.length && args[i].startsWith("-"); i++) { // parse options...
                if ("-user".equals(args[i])) {
                    System.err.println("Warning! '-user' option deprecated now. You should use '-prefix=user:a.b.c'");
                } else
                if ("-system".equals(args[i])) {
                    System.err.println("Warning! '-system' option deprecated now. You should use '-prefix=system:a.b.c'");
                } else
                if ("-both".equals(args[i])) {
                    System.err.println("Warning! '-both' option deprecated now with no direct replacement.");
                } else
                if (args[i].startsWith("-prefix=")) {
                    prefix = args[i].substring(8);
                } else {
                    System.err.println("Unknown option: " + args[i]);
                    System.exit(1);
                }
            }
            processClean(prefix);
        } else {
            System.err.println("Unknown command: " + args[0]);
            System.exit(1);
        }
    }

    private static void processExport(final String expr, final File file) throws IOException, BackingStoreException {
        if (expr == null || expr.isEmpty()) {
            System.out.println("root preference node not specified!");
            return;
        }

        final Preferences node = PreferencesLoader.getPreferenceNode(expr, false);
        if (node != null) {
            if (file != null) {
                final Writer out = new OutputStreamWriter(new FileOutputStream(file), IO_ENCODING);
                try {
                    PreferencesLoader.exportPreferences(node, out);
                } finally {
                    out.close();
                }
            } else {
                PreferencesLoader.exportPreferences(node, System.out);
                System.out.flush();
            }
        }
    }

    private static void processClean(final String expr) {
        if (expr == null || expr.isEmpty()) {
            System.out.println("root preference node not specified!");
            return;
        }
        try {
            final Preferences node = PreferencesLoader.getPreferenceNode(expr, false);
            if (node != null) {
                final Preferences parent = node.parent() != null
                        ? node.parent()
                        : node.isUserNode() ? Preferences.userRoot() : Preferences.systemRoot();
                node.removeNode();
                parent.flush();
                System.out.println("cleaning preferences [" + expr + "] : SUCCESS");
            } else {
                System.out.println("cleaning preferences [" + expr + "] : NOT FOUND");
            }
        } catch (Exception e) {
            System.out.println("cleaning preferences [" + expr + "] : FAILED");
        }
    }

    private static void processImport(final boolean overwrite, final File file) throws IOException {
        if (file != null) {
            final Reader in = new InputStreamReader(new FileInputStream(file), IO_ENCODING);
            try {
                PreferencesLoader.importPreferences(in, overwrite);
            } finally {
                in.close();
            }
        } else {
            PreferencesLoader.importPreferences(new InputStreamReader(System.in, Charset.defaultCharset()), overwrite);
        }
    }
}
