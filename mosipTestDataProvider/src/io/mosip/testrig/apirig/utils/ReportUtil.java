package io.mosip.testrig.apirig.utils;

/**
 * Minimal ReportUtil implementation used by GlobalMethods in this module.
 */
public class ReportUtil {
    public static String getTextAreaForHeaders(String headers) {
        String formattedHeader = "No headers";
        if (headers != null && !headers.isEmpty())
            formattedHeader = headers;
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='padding: 0; margin: 0;'><textarea style='border: solid 1px gray; background-color: lightgray; width: 100%; padding: 0; margin: 0;' name='headers' rows='2' readonly='true'>");
        sb.append(formattedHeader);
        sb.append("</textarea></div>");
        return sb.toString();
    }

    public static String getTextAreaJsonMsgHtml(String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='width: 100%; overflow: hidden;'>");
        sb.append("<textarea style='border:solid 1px black; width: 100%; box-sizing: border-box;' name='message' rows='6' readonly='true'>");
        if (content == null) content = "";
        sb.append(content);
        sb.append("</textarea> </div>");
        return sb.toString();
    }
}
