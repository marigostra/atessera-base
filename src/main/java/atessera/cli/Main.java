// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.cli;

import atessera.markdown.LatexTarget;
import atessera.markdown.HtmlTarget;
import atessera.markdown.MathDefinition;
import atessera.markdown.MathBlockDefinition;
import atessera.markdown.CiteReference;
import atessera.markdown.Reference;
import atessera.markdown.LabelDefinition;
import atessera.markdown.AdvImageDefinition;
import atessera.util.SvgGenerator;

import java.nio.file.*;
import java.util.*;

import com.beust.jcommander.*;

public class Main {
    
    private static class Args {
        @Parameter(names = {"--input", "-i"}, description = "Input markdown file", required = true)
        String inputFile;
        
        @Parameter(names = {"--output", "-o"}, description = "Output file", required = true)
        String outputFile;
        
        @Parameter(names = {"--format", "-f"}, description = "Output format (tex or html)", required = true)
        String format;
        
        @Parameter(names = {"--help", "-h"}, description = "Show help", help = true)
        boolean help;
        
        @Parameter(names = {"--keep-temp"}, description = "Keep temporary files for debugging")
        boolean keepTemp = false;
    }
    
    public static void main(String[] args) {
        Args parsedArgs = new Args();
        JCommander jc = JCommander.newBuilder()
            .addObject(parsedArgs)
            .build();
        
        try {
            jc.parse(args);
            
            if (parsedArgs.help) {
                jc.usage();
                return;
            }
            
            // Устанавливаем базовую директорию для временных файлов
            Path inputPath = Paths.get(parsedArgs.inputFile).toAbsolutePath();
            Path tempBaseDir = inputPath.getParent();
            if (tempBaseDir == null) {
                tempBaseDir = Paths.get(".");
            }
            SvgGenerator.setTempBaseDir(tempBaseDir);
            SvgGenerator.setKeepTempFiles(parsedArgs.keepTemp);
            
            // Проверка зависимостей для HTML формата
            if ("html".equalsIgnoreCase(parsedArgs.format)) {
                if (!SvgGenerator.checkDependencies()) {
                    System.err.println("Error: LaTeX and/or dvisvgm are not installed or not in PATH.");
                    System.err.println("Please install TexLive and dvisvgm:");
                    System.err.println("  Ubuntu/Debian: sudo apt install texlive texlive-latex-extra dvisvgm");
                    System.exit(1);
                }
            }
            
            // Чтение входного файла
            long startTime = System.currentTimeMillis();
            String content = Files.readString(inputPath);
            
            // Обработка на основе формата
            String result;
            
            if ("tex".equalsIgnoreCase(parsedArgs.format)) {
                result = processToTex(content);
            } else if ("html".equalsIgnoreCase(parsedArgs.format)) {
                result = processToHtml(content);
            } else {
                System.err.println("Unsupported format: " + parsedArgs.format);
                System.err.println("Supported formats: tex, html");
                System.exit(1);
                return;
            }
            
            // Запись выходного файла
            Path outputPath = Paths.get(parsedArgs.outputFile);
            Files.writeString(outputPath, result);
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            System.out.println("Converted " + parsedArgs.inputFile + " to " + parsedArgs.outputFile + " in " + totalTime + " ms");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static String processToTex(String content) {
        Set<LatexTarget.Features> features = EnumSet.allOf(LatexTarget.Features.class);
        LatexTarget target = new LatexTarget(features) {
            @Override
            protected String render(MathDefinition math) {
                return "$" + math.getText() + "$";
            }
            
            @Override
            protected String render(MathBlockDefinition math) {
                if (math.getType() == MathBlockDefinition.Type.EQUATION) {
                    if (math.getLabel() != null && !math.getLabel().isEmpty()) {
                        return "\\begin{equation}\n" + math.getText() + "\n\\label{" + math.getLabel() + "}\n\\end{equation}\n";
                    } else {
                        return "\\begin{equation}\n" + math.getText() + "\n\\end{equation}\n";
                    }
                } else {
                    return "\\[" + math.getText() + "\\]\n";
                }
            }
            
            @Override
            protected String render(CiteReference citeRef) {
                return "\\cite{" + citeRef.getRef() + "}";
            }
            
            @Override
            protected String render(Reference ref) {
                if (ref.getType() == Reference.Type.PAGE) {
                    return "\\pageref{" + ref.getRef() + "}";
                } else {
                    return "\\ref{" + ref.getRef() + "}";
                }
            }
            
            @Override
            protected String render(LabelDefinition label) {
                return "\\label{" + label.getLabel() + "}\n";
            }
            
            @Override
            protected String render(AdvImageDefinition advImage) {
                return "\\begin{figure}[h]\n\\centering\n\\includegraphics[width=0.8\\textwidth]{" + 
                       advImage.getSrc() + "}\n\\caption{" + advImage.getAlt() + 
                       (advImage.getComment() != null && !advImage.getComment().isEmpty() ? 
                        " \\textit{" + advImage.getComment() + "}" : "") + "}\n\\end{figure}\n";
            }
            
            @Override
            protected String renderHeadingOpening(int level) {
                switch(level) {
                    case 1: return "\\chapter{";
                    case 2: return "\\section{";
                    case 3: return "\\subsection{";
                    case 4: return "\\subsubsection{";
                    default: return "\\textbf{";
                }
            }
        };
        
        return target.parse(content);
    }
    
    private static String processToHtml(String content) {
        Set<HtmlTarget.Features> features = EnumSet.allOf(HtmlTarget.Features.class);
        HtmlTarget target = new HtmlTarget(features, true);
        
        return target.parseWithStyle(content);
    }
}