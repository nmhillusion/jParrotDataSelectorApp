package tech.nmhillusion.jParrotDataSelectorApp.helper;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-12-07
 */
public class HtmlSanitizer {

    public static String escapeHtml(String text) {
        if (text == null) {
            return null;
        }

        // Initialize with a bit of extra capacity to minimize resizing
        final StringBuilder escapedText = new StringBuilder(text.length() * 2);

        for (int idx = 0; idx < text.length(); idx++) {
            char currChar = text.charAt(idx);
            switch (currChar) {
                case '<':
                    escapedText.append("&lt;");
                    break;
                case '>':
                    escapedText.append("&gt;");
                    break;
                case '&':
                    escapedText.append("&amp;");
                    break;
                case '"':
                    escapedText.append("&quot;");
                    break;
                case '\'':
                    // &apos; is valid in HTML5/XML, but &#39; is safer for legacy browser compatibility
                    escapedText.append("&#39;");
                    break;
                default:
                    // Optimization: You can optionally check for high unicode characters here
                    // if you need to escape emojis or non-ASCII characters.
                    escapedText.append(currChar);
                    break;
            }
        }
        return escapedText.toString();
    }
}
