import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractFromComments {
  private BufferedReader br;
  private String dataInFile;
  private String packageName;
  private String className;
  private String implementedInterfaces;
  private String extendedClass;
  private String introComment;
  private File extractedFile;

  public ExtractFromComments(String sourceFileName) {
    this.dataInFile = getDataInSourceFile(sourceFileName);
    this.packageName = getPackageName(dataInFile);
    this.className = getClassName(dataInFile);
    this.implementedInterfaces = getImplementedInterfaces(dataInFile);
    this.extendedClass = getExtendedClasses(dataInFile);
    this.introComment = getIntroComment(dataInFile);
    this.extractedFile = createFile(className);
    this.attachHeader(extractedFile);
    this.attachInto(extractedFile);
    this.attachConstructorSummary(extractedFile);
    this.attachFunctionSummary(extractedFile);
    this.attachConstructorDetails(extractedFile);
    this.attachFunctionDetails(extractedFile);
    this.createCssFile();
  }

  private String getDataInSourceFile(String sourceFileName) {
    StringBuffer data = new StringBuffer();
    try {
      File file = new File(sourceFileName);
      br = new BufferedReader(new FileReader(file));
      String line = "";
      while ((line = br.readLine()) != null) {
        data.append(line + "\n");
      }
    } catch (IOException e) {
      System.out.println("Source File not found");
    }
    return data.toString();
  }

  private String getPackageName(String dataInFile) {
    String packageRegex = "(package)(\\s*)(java.[(?!)\\w<>]*);";
    Pattern packagePattern = Pattern.compile(packageRegex);
    Matcher packageMatcher = packagePattern.matcher(dataInFile);
    String packageName = "";

    while (packageMatcher.find()) {
      packageName = packageMatcher.group(3);
    }
    return packageName;
  }

  private String getClassName(String dataInFile) {
    String className = "";
    String classNameRegex = "(public)\\s*(class|interface)\\s*([\\w<>]*)\\n" +
        "*\\s*((extends|implements)\\s*([\\w,.<>]*))*\\n" +
        "*\\s*((extends|implements)\\s*([\\w,.<>\\s]*))*";
    Pattern classNamePattern = Pattern.compile(classNameRegex);
    Matcher classNameMatcher = classNamePattern.matcher(dataInFile);
    while (classNameMatcher.find()) {
      className = classNameMatcher.group(3);
    }
    if (className.contains("<")) {
      className.replace("<", "&lt;");
    }
    if (className.contains(">")) {
      className.replace(">", "&gt;");
    }
    return className;
  }

  private String getImplementedInterfaces(String dataInFile) {
    String implementedInterfaces = "";
    String implementedInterfacesRegex = "(public)\\s*(class|interface)\\s*([\\w<>]*)\\n" +
        "*\\s*((extends|implements)\\s*([\\w,.<>]*))*\\n" +
        "*\\s*((extends|implements)\\s*([\\w,.<>\\s]*))*";
    Pattern implementedInterfacesPattern = Pattern.compile(implementedInterfacesRegex);
    Matcher implementedInterfacesMatcher = implementedInterfacesPattern.matcher(dataInFile);

    while (implementedInterfacesMatcher.find()) {
      implementedInterfaces = implementedInterfacesMatcher.group(9);
    }
    return implementedInterfaces;
  }

  private String getExtendedClasses(String dataInFile) {
    String extendedClass = "";
    String extendedClassRegex = "(public)\\s*(class|interface)\\s*([\\w<>]*)\\n" +
        "*\\s*((extends|implements)\\s*([\\w,.<>]*))*\\n" +
        "*\\s*((extends|implements)\\s*([\\w,.<>\\s]*))*";
    Pattern extendedClassPattern = Pattern.compile(extendedClassRegex);
    Matcher extendedClassMatcher = extendedClassPattern.matcher(dataInFile);

    while (extendedClassMatcher.find()) {
      implementedInterfaces = extendedClassMatcher.group(6);
    }
    return extendedClass;
  }

  private String getIntroComment(String dataInFile) {
    String introText = "";
    StringBuffer introComment = new StringBuffer();

    String introCommentRegex = "(\\/\\*\\*\\n)((\\s*\\*\\s)([\\w\\d{}@.\\s\\-_,()<>\\/;\"\"#:=']*))*([\\/\\*]*)";
    Pattern introCommentPattern = Pattern.compile(introCommentRegex);
    Matcher introCommentMatcher = introCommentPattern.matcher(dataInFile);
    introCommentMatcher.find();
    introText = introCommentMatcher.group(0);

    introCommentRegex = "(\\s\\*)(.*)";
    introCommentPattern = Pattern.compile(introCommentRegex);
    introCommentMatcher = introCommentPattern.matcher(introText);
    while (introCommentMatcher.find()) {
      introComment.append(introCommentMatcher.group(2) + "\n");
    }

    return introComment.toString();
  }

  private File createFile(String docTitle) {
    File file = new File("index.html");
    if (file.exists()) {
      return file;
    }

    if (docTitle.contains("<") && docTitle.contains(">")) {
      docTitle = docTitle.replace("<", "&lt;");
      docTitle = docTitle.replace(">", "&gt;");
    }

    FileWriter fileWriter;
    try {
      file.createNewFile();
      try {
        fileWriter = new FileWriter(file);
        fileWriter.write("<!DOCTYPE html>\r\n" +
            "<html lang=\"en\">\r\n" +
            "<head>\r\n" +
            "    <meta charset=\"UTF-8\">\r\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" +
            "    <title>" + docTitle + "</title>\r\n" +
            "    <link rel=\"stylesheet\" href=\"style.css\">" +
            "</head>\r\n" +
            "<body>");
        fileWriter.close();
      } catch (IOException e) {
        System.out.println("Error in file parsing`");
      }
    } catch (IOException e) {
      System.out.println("Error in file parsing");
    }

    return file;
  }

  private void attachHeader(File file) {
    FileWriter fileWriter;
    String implementedInterfacesArray[] = this.implementedInterfaces.split(",\\s");
    String extendedClassesArray[] = this.extendedClass.split(",\\s");
    StringBuffer interfaces = new StringBuffer();
    for (String implementedInterface : implementedInterfacesArray) {
      if (implementedInterface.contains("<"))
        implementedInterface = implementedInterface.replaceAll("<", "&lt;");
      if (implementedInterface.contains(">"))
        implementedInterface = implementedInterface.replaceAll(">", "&gt;");

      interfaces.append("<a href=\\\"https://docs.oracle.com/javase/8/docs/api/java/io/" + implementedInterface
          + ".html\\\" title=\\\"interface in java.io\\\">" + implementedInterface + "</a> ");
    }

    StringBuffer extendedClasses = new StringBuffer();
    for (String extendedClass : extendedClassesArray) {
      if (extendedClass.contains("<"))
        extendedClass = extendedClass.replaceAll("<", "&lt;");
      if (extendedClass.contains(">"))
        extendedClass = extendedClass.replaceAll(">", "&gt;");

      extendedClasses.append("<a href=\\\"https://docs.oracle.com/javase/8/docs/api/java/io/" + extendedClass
          + ".html\\\" title=\\\"interface in java.io\\\">" + extendedClass + "</a> ");
    }

    try {
      fileWriter = new FileWriter(file, true);
      String text = "\n<div class=\"header\">\r\n" + //
          "        <div class=\"subTitle list-header\">In Package:- " + packageName + "</div>\r\n" + //
          "        <h2 title=\"" + className + "\" class=\"title mb-10 mt-10 monospace grey\">" + className
          + "</h2>\r\n" + //
          "        <dl>\r\n" + //
          "            <dt class=\"p-5 list-header\">All Implemented Interfaces:</dt>\r\n" + //
          "            <dd class=\"p-5\">" + interfaces + "</dd>\r\n" + //
          "        </dl>\r\n" + //
          "        <dl>\r\n" + //
          "            <dt class=\"p-5 list-header\">Extended Classes:</dt>\r\n" + //
          "            <dd class=\"p-5\">" + extendedClasses + "</dd>\r\n" + //
          "        </dl>\r\n" + //
          "</div><hr class=\"mb-10\">";
      fileWriter.append(text.replaceAll("\\\\\"", "\""));
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void attachInto(File file) {
    int currentPosition = 0;
    FileWriter fileWriter;
    String htmlText = "";
    String aTag = "";
    String regex = "\\{@(\\w*)\\s([#\\(\\)<>\\w.=\\d\\-\\|,\\*]*)(\\s[\\w.\\,<>(\\)#]*)*\\}";
    Pattern patternRegex = Pattern.compile(regex);
    Matcher patternMatcher = patternRegex.matcher(regex);
    StringBuffer alteredIntroText = new StringBuffer();
    int finalPatternIndex = 0;

    try {
      fileWriter = new FileWriter(file, true);
      if (introComment.contains("@code") || introComment.contains("@link")) {
        while (patternMatcher.find()) {
          int start = patternMatcher.start();
          int end = patternMatcher.end();
          String identifier = patternMatcher.group(1);

          if (identifier.equalsIgnoreCase("code")) {
            String codeText = patternMatcher.group(2);
            alteredIntroText.append(introComment.substring(currentPosition, start));
            alteredIntroText.append("<i>" + codeText + "</i>");
            currentPosition = end;
            finalPatternIndex = end;
          }

          if (identifier.equalsIgnoreCase("link")) {
            identifier = patternMatcher.group(1);
            String href = patternMatcher.group(2);
            alteredIntroText.append(introComment.substring(currentPosition, start));
            aTag = "<a href = \\\"" + href + "\\\"></a>";
            try {
              htmlText = patternMatcher.group(3);
              aTag = "<a href = \\\"" + href + "\\\">" + htmlText + "</a>";
            } catch (Exception e) {
              System.out.println("Error in parsing");
            }
            alteredIntroText.append(aTag);
            currentPosition = end;
            finalPatternIndex = end;
          }
        }
      }

      alteredIntroText.append(introComment.substring(finalPatternIndex, introComment.length()));
      String intro = alteredIntroText.toString().replaceAll("@param", "");
      intro = intro.replaceAll("@author", "\n <i class=\"line-list list-header mt-10\">Author</i>");
      intro = intro.replaceAll("@see", "\n <i class=\"line-list list-header mt-10\">Also see</i>");
      intro = intro.replaceAll("@since", "\n <i class=\"line-list list-header mt-10\">Since</i>");
      intro = intro.substring(0, intro.length() - 3);

      try {
        fileWriter = new FileWriter(file, true);
        String text = "<div class=\"introText mt-10\">\r\n" + //
            "<p>" + intro + "</p>\r\n" + //
            "</div>";
        fileWriter.append(text.replaceAll("\\\\\"", "\""));
        fileWriter.close();
      } catch (IOException e) {
        System.out.println("Error in parsing file");
      }
    } catch (Exception e) {
      System.out.println("Error in parsing file");
    }
  }

  private void attachConstructorSummary(File file) {
    FileWriter fileWriter;
    String constructorName = "";
    String constructorRegex = "(\\/\\*\\*)([-=&|:?#%(<\\/>),'\\w\\s\\*@.]*)(\\s*\\*\\/)\\s*(public)\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*)\\s*(\\(.*\\))";
    Pattern constructorPattern = Pattern.compile(constructorRegex);
    Matcher constructorMatcher = constructorPattern.matcher(dataInFile);
    StringBuffer htmlCode = new StringBuffer();
    htmlCode.append("<div class=\"constructorSummary mt-20 p-10\"> " + " \n");
    htmlCode.append("<h5><i>Constructor Summary</i></h5>" + " \n");
    htmlCode.append("<ul class=\"mt-20\"><h6 class=\"summary-header\">Constructors:-</h6>" + " \n");

    while (constructorMatcher.find()) {
      String functionRegex = "(public)\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*)\\s*(\\(.*\\))";
      Pattern functionPattern = Pattern.compile(functionRegex);
      Matcher functionMatcher = functionPattern.matcher(constructorMatcher.group(0));
      while (functionMatcher.find()) {
        if (className.contains(functionMatcher.group(2))) {
          constructorName = functionMatcher.group(0);
          if (functionMatcher.group(0).contains("<"))
            constructorName = constructorName.replaceAll("<", "&lt;");
          if (functionMatcher.group(0).contains(">"))
            constructorName = constructorName.replaceAll(">", "&gt;");
          String comments = constructorMatcher.group(2);
          comments = comments.replaceAll("\\*", "");
          String comment[] = comments.split("\\.");
          comments = comment[0];

          htmlCode.append("<li class=\"p-10\"><a href=\"#" + functionMatcher.group(0) + "\"><p class=\"method-header\">"
              + constructorName + "</a>\r\n" + //
              "<p>" + comments + "</p>\r\n" + //
              "</li> " + " \r\n");
        }
      }
    }
    htmlCode.append("</ul>\r\n" + //
        "</div>");

    try {
      fileWriter = new FileWriter(file, true);
      fileWriter.append(htmlCode);
      fileWriter.close();
    } catch (IOException e) {
      System.out.println("Error in parsing");
    }
  }

  private void attachFunctionSummary(File f) {
    FileWriter fileWriter;
    String funcName = "";
    String funcRegex = "(\\/\\*\\*)([-=&|:;?#%(<\\/>),'\\w\\s\\*@.]*)(\\s*\\*\\/)\\s*(public)\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*)\\s*(\\(.*\\))";
    Pattern funcPattern = Pattern.compile(funcRegex);
    Matcher funcMatcher = funcPattern.matcher(dataInFile);
    StringBuffer htmlCode = new StringBuffer();
    htmlCode.append("<div class=\"functionSummary  mt-20 p-10\">  \n");
    htmlCode.append("<h5><i class=\"method-header\">Method Summary</i></h5>  \n");
    htmlCode.append("<table class=\"mt-20\"> \n");
    htmlCode.append("<tr class=\"summary-header \">\r\n" + //
        "                        <th class=\"return-column p-10\" style=\"text-align: left;\">Return type</th>\r\n" + //
        "                        <th>Function</th>\r\n" + //
        "                    </tr>\r\n");
    while (funcMatcher.find()) {
      String functionRegex = "(public)\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*)\\s*(\\(.*\\))";
      Pattern functionPattern = Pattern.compile(functionRegex);
      Matcher functionMatcher = functionPattern.matcher(funcMatcher.group(0));
      while (functionMatcher.find()) {
        if (!className.contains(functionMatcher.group(2))) {
          funcName = functionMatcher.group(0);
          if (functionMatcher.group(0).contains("<"))
            funcName = funcName.replaceAll("<", "&lt;");
          if (functionMatcher.group(0).contains(">"))
            funcName = funcName.replaceAll(">", "&gt;");
          String comments = funcMatcher.group(2);
          comments = comments.replaceAll("\\*", "");
          String comment[] = comments.split("\\.");
          comments = comment[0];
          if (comments.contains(functionMatcher.group(0))) {
            int index = comments.indexOf(functionMatcher.group(0));
            if (index != -1) {
              comment = comments.split("public");
              comments = comment[0];
            }
          }
          htmlCode.append("<tr>\r\n" + //
              "                        <td>" + functionMatcher.group(2) + "</td>\r\n" + //
              "                        <td><a href=\"#" + functionMatcher.group(0) + "\"><p class=\"method-header\">"
              + functionMatcher.group(0) + "</p></a>\r\n" + //
              "                            <p>" + comments + "</p>\r\n" + //
              "                       </td>\r\n" + //
              "                    </tr>\r\n");
        }
      }

    }
    htmlCode.append("</table>\r\n" + //
        "            </div>");

    try {

      fileWriter = new FileWriter(f, true);
      fileWriter.append(htmlCode);
      fileWriter.close();

    } catch (IOException e) {
      System.out.println("Error in parsing");
    }
  }

  private void attachConstructorDetails(File f) {
    FileWriter fileWriter;
    String constructorName = "";
    String constructorRegex = "(\\/\\*\\*)([-=&|:?#%(<\\/>),'\\w\\s\\*@.]*)(\\s*\\*\\/)\\s*(public)\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*)\\s*(\\(.*\\))";
    Pattern constructorPattern = Pattern.compile(constructorRegex);
    Matcher constructorMatcher = constructorPattern.matcher(dataInFile);
    StringBuffer htmlCode = new StringBuffer();
    htmlCode.append("<div class=\"constructorSummary mt-20 p-10\"> \n");
    htmlCode.append("<h5><i>Constructor Details</i></h5> \n");
    htmlCode.append("<ul class=\"mt-20\">\n");
    while (constructorMatcher.find()) {
      String functionRegex = "(public)\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*)\\s*(\\(.*\\))";
      Pattern functionPattern = Pattern.compile(functionRegex);
      Matcher functionMatcher = functionPattern.matcher(constructorMatcher.group(0));
      while (functionMatcher.find()) {
        if (className.contains(functionMatcher.group(2))) {
          constructorName = functionMatcher.group(0);
          if (functionMatcher.group(0).contains("<")) {
            constructorName = constructorName.replaceAll("<", "&lt;");
          }
          if (functionMatcher.group(0).contains(">"))
            constructorName = constructorName.replaceAll(">", "&gt;");
          String comments = constructorMatcher.group(2);
          comments = comments.replaceAll("\\*", "");
          String comment[] = comments.split("\\.");
          comments = comment[0];
          htmlCode.append("<li class=\"p-10\" id=\"" + functionMatcher.group(0)
              + "\"><p class=\"method-header summary-header\">" + functionMatcher.group(0) + "</p>\n");
          htmlCode.append("<p class=\"p-10 bg-white\">" + comments + "</p>\n");
          htmlCode.append("</li>\n");

        }
      }
    }
    htmlCode.append("</ul>\n");
    htmlCode.append("</div>\n");
    try {

      fileWriter = new FileWriter(f, true);
      fileWriter.append(htmlCode);
      fileWriter.close();
    } catch (IOException e) {
      System.out.println("Error in parsing");
    }
  }

  private void attachFunctionDetails(File f) {
    FileWriter fileWriter;
    String funcName = "";
    String funcRegex = "(\\/\\*\\*)([-=&|:;?#%(<\\/>),'\\w\\s\\*@.]*)(\\s*\\*\\/)\\s*(public)\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*)\\s*(\\(.*\\))";
    Pattern funcPattern = Pattern.compile(funcRegex);
    Matcher funcMatcher = funcPattern.matcher(dataInFile);
    StringBuffer htmlCode = new StringBuffer();
    htmlCode.append("<div class=\"constructorSummary mt-20 p-10\"> \n");
    htmlCode.append("<h5><i>Method Details</i></h5>  \n");
    htmlCode.append("<ul class=\"mt-20\">\n");
    while (funcMatcher.find()) {
      String functionRegex = "(public)\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*){0,1}\\s*([\\w<>\\[\\]]*)\\s*(\\(.*\\))";
      Pattern functionPattern = Pattern.compile(functionRegex);
      Matcher functionMatcher = functionPattern.matcher(funcMatcher.group(0));
      while (functionMatcher.find()) {
        if (className.contains(functionMatcher.group(2))) {

        } else {
          funcName = functionMatcher.group(0);
          if (functionMatcher.group(0).contains("<")) {
            funcName = funcName.replaceAll("<", "&lt;");
          }
          if (functionMatcher.group(0).contains(">"))
            funcName = funcName.replaceAll(">", "&gt;");
          String comments = funcMatcher.group(2);
          comments = comments.replaceAll("\\*", "");

          comments = comments.replaceAll("@param",
              "\n <span class=\"keyword mt-20 method-header\">Parameters :-</span> \n");
          comments = comments.replaceAll("@throws",
              "\n <span class=\"keyword mt-20 method-header\">Throws Exception :-</span> \n");
          comments = comments.replaceAll("@return",
              "\n <span class=\"keyword mt-20 method-header\">Returns :-</span> \n");

          htmlCode.append("<li class=\"p-10\" id=\"" + funcName + "\"><p class=\"method-header summary-header\">"
              + funcName + "</p>\n");
          htmlCode.append("<p class=\"p-10 bg-white\">" + comments.replaceAll("<p>", "") + "</p>\n");
          htmlCode.append("</li>\n");
        }
      }
    }

    htmlCode.append("</ul>\n");
    htmlCode.append("</div>\n");
    try {
      fileWriter = new FileWriter(f, true);
      fileWriter.append(htmlCode);
      fileWriter.close();
    } catch (IOException e) {
      System.out.println("Error in parsing");
    }
  }

  private void createCssFile() {
    FileWriter fileWriter;
    File cssFile;
    StringBuffer cssContent = new StringBuffer();

    cssContent.append("* {\n")
              .append("  padding: 0;\n")
              .append("  margin: 0;\n")
              .append("  box-sizing: border-box;\n")
              .append("}\n\n")
              .append(".monospace {\n")
              .append("  font-family: Verdana, Geneva, Tahoma, sans-serif;\n")
              .append("}\n\n")
              .append("a {\n")
              .append("  text-decoration: none;\n")
              .append("  color: #4A6782;\n")
              .append("  font-family: helvetica;\n")
              .append("  font-size: medium;\n")
              .append("}\n\n")
              .append("a:hover {\n")
              .append("  color: #b86d67;\n")
              .append("}\n\n")
              .append("body {\n")
              .append("  word-spacing: 5px;\n")
              .append("  font-size: medium;\n")
              .append("  font-weight: lighter;\n")
              .append("  font-family: Arial, Helvetica, sans-serif;\n")
              .append("  line-height: 1.5;\n")
              .append("  padding: 20px;\n")
              .append("  color: #2c4557;\n")
              .append("}\n\n")
              .append("h1, h2, h3, h4, h5, h6 {\n")
              .append("  font-family: 'Montserrat', sans-serif;\n")
              .append("}\n\n")
              .append("h1 { font-size: 2.7rem; }\n")
              .append("h2 { font-size: 2.4rem; }\n")
              .append("h3 { font-size: 2.1rem; }\n")
              .append("h4 { font-size: 1.8rem; }\n")
              .append("h5 { font-size: 1.5rem; }\n")
              .append("h6 { font-size: 1.2rem; }\n\n")
              .append(".p-0 { padding: 0; }\n")
              .append(".p-5 { padding: 5px; }\n")
              .append(".p-10 { padding: 10px; }\n")
              .append(".p-15 { padding: 15px; }\n")
              .append(".p-20 { padding: 20px; }\n")
              .append(".p-30 { padding: 30px; }\n")
              .append(".p-40 { padding: 40px; }\n")
              .append(".p-50 { padding: 50px; }\n")
              .append(".p-60 { padding: 60px; }\n\n")
              .append(".pt-0 { padding-top: 0; }\n")
              .append(".pt-10 { padding-top: 10px; }\n")
              .append(".pt-15 { padding-top: 15px; }\n")
              .append(".pt-20 { padding-top: 20px; }\n")
              .append(".pt-30 { padding-top: 30px; }\n")
              .append(".pt-40 { padding-top: 40px; }\n\n")
              .append(".pb-0 { padding-bottom: 0; }\n")
              .append(".pb-10 { padding-bottom: 10px; }\n")
              .append(".pb-15 { padding-bottom: 15px; }\n")
              .append(".pb-20 { padding-bottom: 20px; }\n")
              .append(".pb-30 { padding-bottom: 30px; }\n")
              .append(".pb-40 { padding-bottom: 40px; }\n\n")
              .append(".ps-0 { padding-left: 0; }\n")
              .append(".ps-10 { padding-left: 10px; }\n")
              .append(".ps-15 { padding-left: 15px; }\n")
              .append(".ps-20 { padding-left: 20px; }\n")
              .append(".ps-30 { padding-left: 30px; }\n")
              .append(".ps-40 { padding-left: 40px; }\n\n")
              .append(".pe-0 { padding-right: 0; }\n")
              .append(".pe-10 { padding-right: 10px; }\n")
              .append(".pe-15 { padding-right: 15px; }\n")
              .append(".pe-20 { padding-right: 20px; }\n")
              .append(".pe-30 { padding-right: 30px; }\n")
              .append(".pe-40 { padding-right: 40px; }\n\n")
              .append(".m-0 { margin: 0; }\n")
              .append(".m-10 { margin: 10px; }\n")
              .append(".m-15 { margin: 15px; }\n")
              .append(".m-20 { margin: 20px; }\n")
              .append(".m-30 { margin: 30px; }\n")
              .append(".m-40 { margin: 40px; }\n\n")
              .append(".mt-0 { margin-top: 0; }\n")
              .append(".mt-10 { margin-top: 10px; }\n")
              .append(".mt-15 { margin-top: 15px; }\n")
              .append(".mt-20 { margin-top: 20px; }\n")
              .append(".mt-30 { margin-top: 30px; }\n")
              .append(".mt-40 { margin-top: 40px; }\n\n")
              .append(".mb-0 { margin-bottom: 0; }\n")
              .append(".mb-10 { margin-bottom: 10px; }\n")
              .append(".mb-15 { margin-bottom: 15px; }\n")
              .append(".mb-20 { margin-bottom: 20px; }\n")
              .append(".mb-30 { margin-bottom: 30px; }\n")
              .append(".mb-40 { margin-bottom: 40px; }\n\n")
              .append(".ms-0 { margin-left: 0; }\n")
              .append(".ms-10 { margin-left: 10px; }\n")
              .append(".ms-15 { margin-left: 15px; }\n")
              .append(".ms-20 { margin-left: 20px; }\n")
              .append(".ms-30 { margin-left: 30px; }\n")
              .append(".ms-40 { margin-left: 40px; }\n\n")
              .append(".me-0 { margin-right: 0; }\n")
              .append(".me-10 { margin-right: 10px; }\n")
              .append(".me-15 { margin-right: 15px; }\n")
              .append(".me-20 { margin-right: 20px; }\n")
              .append(".me-30 { margin-right: 30px; }\n")
              .append(".me-40 { margin-right: 40px; }\n\n")
              .append(".functionSummary {\n")
              .append("  background-color: #EEEEEF;\n")
              .append("  padding: 1rem;\n")
              .append("}\n\n")
              .append("tbody {\n")
              .append("  background-color: #f8f8f8;\n")
              .append("}\n\n")
              .append("tbody:nth-child(2n+1) {\n")
              .append("  background-color: #EEEEEF;\n")
              .append("}\n\n")
              .append("tbody:nth-child(2n) {\n")
              .append("  background-color: #f8f8f8;\n")
              .append("}\n\n")
              .append("ul li {\n")
              .append("  list-style: none;\n")
              .append("}\n\n")
              .append(".blue-tone {\n")
              .append("  color: #2c4557;\n")
              .append("}\n\n")
              .append(".list-header {\n")
              .append(
                  "  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;\n")
              .append("  font-size: 15px;\n")
              .append("  font-weight: 500;\n")
              .append("}\n\n")
              .append(".strong, tt {\n")
              .append("  font-size: large;\n")
              .append("  font-weight: bold;\n")
              .append("}\n\n")
              .append(".line-list {\n")
              .append("  display: block;\n")
              .append("}\n\n")
              .append(".constructorSummary {\n")
              .append("  /* border: 1px solid black; */\n")
              .append("  background-color: rgb(243, 243, 243);\n")
              .append("}\n\n")
              .append("li:nth-child(2n), tr:nth-child(2n) {\n")
              .append("  background-color: white;\n")
              .append("}\n\n")
              .append(".method-header {\n")
              .append("  color: #4A6782;\n")
              .append("  font-weight: bold;\n")
              .append("}\n\n")
              .append(".summary-header {\n")
              .append("  padding: 10px;\n")
              .append("  background-color: rgb(199, 213, 224);\n")
              .append("}\n\n")
              .append(".return-column {\n")
              .append("  width: 10%;\n")
              .append("}\n\n")
              .append("td {\n")
              .append("  padding-left: 10px;\n")
              .append("}\n\n")
              .append(".bg-white {\n")
              .append("  background-color: white;\n")
              .append("}\n\n")
              .append(".keyword {\n")
              .append("  display: block;\n")
              .append("}\n");

   try {
    cssFile = new File("style.css");
    fileWriter = new FileWriter(cssFile);
    fileWriter.append(cssContent.toString());

    fileWriter.close();
   } catch(IOException e) {
    System.out.println("Error in parsing");
   }
  }

  public File getExtractedFile() {
    return extractedFile;
  }
}
