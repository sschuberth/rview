/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview.misc;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    public static final String NON_PRINTABLE_CHAR = "\u0001";
    public static final String NON_PRINTABLE_CHAR2 = "\u0002";

    private static final Pattern A_NON_WORD_CHARACTER_AT_END
            = Pattern.compile(".*[^[0-9a-zA-Z],;]", Pattern.MULTILINE);
    private static final Pattern A_NON_WORD_CHARACTER_AT_START
            = Pattern.compile("[^[a-zA-Z]].*", Pattern.MULTILINE);
    private static final Pattern NON_WHITESPACE = Pattern.compile("\\w");

    private static final String QUOTE_START_TAG = "[QUOTE]";
    private static final String QUOTE_END_TAG = "[/QUOTE]";

    private static final Pattern QUOTE_REGEXP
            = Pattern.compile("^\\[QUOTE\\](.+?)\\[/QUOTE\\]$", Pattern.MULTILINE | Pattern.DOTALL);


    private static final Pattern QUOTE1 = Pattern.compile("^> ", Pattern.MULTILINE);
    private static final Pattern QUOTE2 = Pattern.compile("^>", Pattern.MULTILINE);
    private static final Pattern QUOTE3 = Pattern.compile("^ > ", Pattern.MULTILINE);
    private static final Pattern QUOTE4 = Pattern.compile("^ >", Pattern.MULTILINE);
    private static final Pattern REPLACED_QUOTE1 = Pattern.compile(NON_PRINTABLE_CHAR + ">");
    private static final Pattern REPLACED_QUOTE2 = Pattern.compile(NON_PRINTABLE_CHAR + " > ");
    private static final Pattern REPLACED_QUOTE3 = Pattern.compile(NON_PRINTABLE_CHAR + " ");
    private static final Pattern REPLACED_QUOTE4 = Pattern.compile(NON_PRINTABLE_CHAR + "\n");

    public static final Pattern GERRIT_CHANGE = Pattern.compile("I[0-9a-f]{8,40}");
    public static final Pattern GERRIT_CHANGE_ID = Pattern.compile("\\d+");
    public static final Pattern GERRIT_COMMIT = Pattern.compile("[0-9a-f]{7,40}");

    public static String[] obtainParagraphs(String message) {
        return message.split("\\r?\\n\\r?\\n");
    }

    public static boolean isQuote(String p) {
        return p.startsWith("> ") || p.startsWith(" > ");
    }

    public static boolean isPreFormat(final String p) {
        return p.contains("\n ") || p.contains("\n\t") || p.startsWith(" ")
                || p.startsWith("\t");
    }

    public static boolean isList(final String p) {
        return p.contains("\n- ") || p.contains("\n* ") || p.startsWith("- ")
                || p.startsWith("* ") || p.startsWith(NON_PRINTABLE_CHAR + "- ")
                || p.startsWith(NON_PRINTABLE_CHAR + "* ");
    }

    public static String removeLineBreaks(String message) {
        String[] lines = message.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        int count = lines.length;
        for (int i = 0; i < count; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            sb.append(line);
            if (trimmed.isEmpty() || A_NON_WORD_CHARACTER_AT_END.matcher(trimmed).matches()) {
                sb.append("\n");
            } else if (i < (count - 1)) {
                String next = lines[i + 1].trim();
                if (next.isEmpty() || A_NON_WORD_CHARACTER_AT_START.matcher(next).matches()) {
                    sb.append("\n");
                } else {
                    sb.append(" ");
                }
            }
        }
        return sb.toString().trim();
    }

    public static String obtainQuote(String message) {
        String msg = QUOTE1.matcher(message).replaceAll(NON_PRINTABLE_CHAR);
        msg = QUOTE2.matcher(msg).replaceAll(NON_PRINTABLE_CHAR);
        msg = QUOTE3.matcher(msg).replaceAll(NON_PRINTABLE_CHAR);
        msg = QUOTE4.matcher(msg).replaceAll(NON_PRINTABLE_CHAR);
        msg = REPLACED_QUOTE1.matcher(msg).replaceAll(NON_PRINTABLE_CHAR + NON_PRINTABLE_CHAR);
        msg = REPLACED_QUOTE2.matcher(msg).replaceAll(NON_PRINTABLE_CHAR + NON_PRINTABLE_CHAR);
        msg = REPLACED_QUOTE3.matcher(msg).replaceAll(NON_PRINTABLE_CHAR);
        msg = REPLACED_QUOTE4.matcher(msg).replaceAll(NON_PRINTABLE_CHAR + " \n");
        return msg;
    }

    public static String obtainQuoteFromMessage(String msg) {
        Matcher matcher = QUOTE_REGEXP.matcher(msg);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            int last = 0;
            do {
                int start = matcher.start(1) - QUOTE_START_TAG.length();
                int end = matcher.end(1) + QUOTE_END_TAG.length();
                if (last < start) {
                    sb.append(msg.substring(last, start));
                }

                String quote = (" > " + matcher.group(1))
                        .replaceAll("\n", "\n > ")
                        .replaceAll(NON_PRINTABLE_CHAR, " > ");
                while (true) {
                    String m = quote;
                    quote = quote.replaceAll(">  >", "> >");
                    if (quote.equals(m)) {
                        break;
                    }
                }
                sb.append(quote);
                last = end;
            } while (matcher.find());
            if (last < msg.length()) {
                sb.append(msg.substring(last));
            }
            return sb.toString();
        }
        return msg;
    }

    public static String obtainPreFormatMessage(String msg) {
        // Remove any pre-formated whitespace
        final Matcher matcher = NON_WHITESPACE.matcher(msg);
        if (matcher.find() && matcher.start() > 0) {
            final String r = (String.format("%1$-" + matcher.start() + "s", " "));
            final String t = (String.format("%1$-" + matcher.start() + "s", "\t"));
            msg = msg.replaceFirst(r, "").replaceAll("\n" + r, "\n")
                    .replaceFirst(t, "").replaceAll("\n" + t, "\n");
        }

        return NON_PRINTABLE_CHAR2 + msg + NON_PRINTABLE_CHAR2;
    }

    public static String quoteMessage(String prev, String msg) {
        if (msg.contains("\n\n")) {
            msg = msg.substring(msg.indexOf("\n\n") + 2);
        }
        msg = QUOTE_START_TAG + StringHelper.obtainQuote(msg)  + QUOTE_END_TAG + "\n\n";
        if (!TextUtils.isEmpty(prev) && !prev.endsWith("\n")) {
            msg = prev + "\n" + msg;
        } else {
            msg = prev + msg;
        }
        return msg;
    }

    public static String removeExtraLines(String msg) {
        if (msg.length() == 0) {
            return msg;
        }
        int p = msg.length();
        while (p > 0 && (msg.charAt(p - 1) == '\n' || msg.charAt(p - 1) == '\r')) {
            p--;
        }
        return msg.substring(0, p);
    }

    public static int countOccurrences(String find, String in) {
        return countOccurrences(find, in, 0, in.length());
    }

    public static int countOccurrences(String find, String in, int start, int end) {
        int count = 0;
        Pattern pattern = Pattern.compile(find);
        Matcher matcher = pattern.matcher(in);
        while (matcher.find()) {
            if (matcher.start() >= start && matcher.end() <= end) {
                count++;
            }
        }
        return count;
    }

    public static Uri buildUriAndEnsureScheme(String src) {
        Uri uri = Uri.parse(src);
        if (uri.getScheme() == null) {
            // Assume we are talking about an http url
            uri = Uri.parse("http://" + src);
        }
        return uri;
    }

    public static String getSafeLastPathSegment(Uri uri) {
        if (uri.getLastPathSegment() != null) {
            return uri.getLastPathSegment();
        }

        String u = uri.toString();
        if (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        int pos = u.lastIndexOf("/", 9);
        if (pos != -1) {
            return u.substring(u.lastIndexOf("/") + 1);
        }
        return null;
    }

    public static String getFileExtension(File file) {
        final String name = file.getName();
        int pos = name.lastIndexOf(".");
        if (pos != -1 && !name.endsWith(".")) {
            return name.substring(pos + 1);
        }
        return null;
    }

    public static String getMimeType(File file) {
        // Extract the mime/type of the file
        String ext = StringHelper.getFileExtension(file);
        String mediaType = null;
        if (ext != null) {
            mediaType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        }
        if (mediaType == null) {
            mediaType = "application/octet-stream";
        }
        return mediaType;
    }
}
