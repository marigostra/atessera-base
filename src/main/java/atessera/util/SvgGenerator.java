// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class SvgGenerator {
    
    private static final String TEMP_PREFIX = "atessera_math_";
    private static final long TIMEOUT_SECONDS = 30;
    private static final double SCALE_FACTOR = 1.5;
    
    private static Path tempBaseDir = null;
    private static boolean keepTempFiles = false;
    private static int formulaCounter = 0;

    public static void setTempBaseDir(Path baseDir) {
        tempBaseDir = baseDir;
    }
    
    public static void setKeepTempFiles(boolean keep) {
        keepTempFiles = keep;
    }
    
    public static synchronized String generateSvg(String latexExpression) throws Exception {
        Path tempDir = null;
        int currentFormulaId = ++formulaCounter;
        
        try {
            if (tempBaseDir != null) {
                String uniqueId = System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "_" + 
                                 UUID.randomUUID().toString().substring(0, 8);
                tempDir = tempBaseDir.resolve(TEMP_PREFIX + uniqueId);
                Files.createDirectories(tempDir);
            } else {
                tempDir = Files.createTempDirectory(TEMP_PREFIX);
            }
            
            Path texFile = tempDir.resolve("formula.tex");
            
            String texContent = "\\documentclass[preview]{standalone}\n" +
                               "\\usepackage{amsmath}\n" +
                               "\\usepackage{amssymb}\n" +
                               "\\usepackage{amsfonts}\n" +
                               "\\begin{document}\n" +
                               latexExpression + "\n" +
                               "\\end{document}\n";
            
            Files.writeString(texFile, texContent);
            
            ProcessBuilder latexPb = new ProcessBuilder("latex", "-interaction=nonstopmode", 
                    "-output-directory=" + tempDir.toString(), texFile.toString());
            latexPb.redirectErrorStream(true);
            Process latexProcess = latexPb.start();
            
            boolean latexFinished = latexProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!latexFinished) {
                latexProcess.destroyForcibly();
                throw new RuntimeException("LaTeX process timed out");
            }
            
            if (latexProcess.exitValue() != 0) {
                throw new RuntimeException("LaTeX failed");
            }
            
            Path dviFile = tempDir.resolve("formula.dvi");
            if (!Files.exists(dviFile)) {
                throw new RuntimeException("DVI file not generated");
            }
            
            Path svgFile = tempDir.resolve("formula.svg");
            ProcessBuilder dvisvgmPb = new ProcessBuilder("dvisvgm", "--no-fonts", "--exact", 
                    "--output=" + svgFile.toString(), dviFile.toString());
            dvisvgmPb.redirectErrorStream(true);
            Process dvisvgmProcess = dvisvgmPb.start();
            
            boolean dvisvgmFinished = dvisvgmProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!dvisvgmFinished) {
                dvisvgmProcess.destroyForcibly();
                throw new RuntimeException("dvisvgm process timed out");
            }
            
            if (dvisvgmProcess.exitValue() != 0) {
                throw new RuntimeException("dvisvgm failed");
            }
            
            if (!Files.exists(svgFile)) {
                throw new RuntimeException("SVG file not generated");
            }
            
            String svgContent = Files.readString(svgFile);
            svgContent = scaleSvgSize(svgContent);
            svgContent = makeIdsUnique(svgContent, currentFormulaId);
            
            return svgContent;
            
        } finally {
            if (!keepTempFiles && tempDir != null) {
                cleanupTempFiles(tempDir);
            }
        }
    }
    
    private static String scaleSvgSize(String svg) {
        Pattern widthPattern = Pattern.compile("width='([0-9.]+)pt'");
        Pattern heightPattern = Pattern.compile("height='([0-9.]+)pt'");
        
        Matcher widthMatcher = widthPattern.matcher(svg);
        Matcher heightMatcher = heightPattern.matcher(svg);
        
        StringBuffer result = new StringBuffer();
        
        if (widthMatcher.find()) {
            double oldWidth = Double.parseDouble(widthMatcher.group(1));
            double newWidth = oldWidth * SCALE_FACTOR;
            String formattedWidth = String.format(Locale.US, "%.6f", newWidth);
            widthMatcher.appendReplacement(result, "width='" + formattedWidth + "pt'");
        }
        widthMatcher.appendTail(result);
        
        StringBuffer result2 = new StringBuffer();
        heightMatcher = heightPattern.matcher(result.toString());
        if (heightMatcher.find()) {
            double oldHeight = Double.parseDouble(heightMatcher.group(1));
            double newHeight = oldHeight * SCALE_FACTOR;
            String formattedHeight = String.format(Locale.US, "%.6f", newHeight);
            heightMatcher.appendReplacement(result2, "height='" + formattedHeight + "pt'");
        }
        heightMatcher.appendTail(result2);
        
        return result2.toString();
    }
    
    private static String makeIdsUnique(String svg, int formulaId) {
        String prefix = "svg" + formulaId + "_";
        
        Pattern idPattern = Pattern.compile("id='([^']+)'");
        Matcher idMatcher = idPattern.matcher(svg);
        StringBuffer result = new StringBuffer();
        
        while (idMatcher.find()) {
            String oldId = idMatcher.group(1);
            idMatcher.appendReplacement(result, "id='" + prefix + oldId + "'");
        }
        idMatcher.appendTail(result);
        
        Pattern hrefPattern = Pattern.compile("xlink:href='#([^']+)'");
        Matcher hrefMatcher = hrefPattern.matcher(result.toString());
        StringBuffer result2 = new StringBuffer();
        
        while (hrefMatcher.find()) {
            String oldRef = hrefMatcher.group(1);
            hrefMatcher.appendReplacement(result2, "xlink:href='#" + prefix + oldRef + "'");
        }
        hrefMatcher.appendTail(result2);
        
        return result2.toString();
    }
    
    private static void cleanupTempFiles(Path tempDir) {
        if (tempDir != null && Files.exists(tempDir)) {
            try {
                Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    public static boolean checkDependencies() {
        try {
            ProcessBuilder latexPb = new ProcessBuilder("latex", "--version");
            Process latexProcess = latexPb.start();
            boolean latexOk = latexProcess.waitFor(5, TimeUnit.SECONDS) && latexProcess.exitValue() == 0;
            
            ProcessBuilder dvisvgmPb = new ProcessBuilder("dvisvgm", "--version");
            Process dvisvgmProcess = dvisvgmPb.start();
            boolean dvisvgmOk = dvisvgmProcess.waitFor(5, TimeUnit.SECONDS) && dvisvgmProcess.exitValue() == 0;
            
            return latexOk && dvisvgmOk;
        } catch (Exception e) {
            return false;
        }
    }
}