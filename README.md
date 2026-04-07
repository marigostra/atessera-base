# Atessera-Base - Markdown to HTML/TeX Converter with Math SVG Support

## About

This tool converts Markdown documents with mathematical formulas to HTML or TeX formats. Features:

- Inline formulas: `[$ ... ]`
- Block formulas: `$$ ... $$`
- Block formulas with labels for references: `$$ ... $$(label)`
- Converts math formulas to SVG for HTML output
- Generates TeX for scientific publications

## Dependencies

### 1. JDK 17

Download and install JDK 17:
[https://adoptium.net/temurin/releases?version=17&os=any&arch=any](https://adoptium.net/temurin/releases?version=17&os=any&arch=any)

### 2. TeX Live

Install TeX Live:
[https://tug.org/texlive/](https://tug.org/texlive/)

### 3. Gradle

Install Gradle:
[https://docs.gradle.org/current/userguide/installation.html](https://docs.gradle.org/current/userguide/installation.html)

## Build

```bash
gradle build
```

## Run

### Convert to HTML (with SVG formulas)

```bash
gradle run --args="-i example.md -o output.html -f html"
```

### Convert to TeX

```bash
gradle run --args="-i example.md -o output.tex -f tex"
```

### Command line options

* `-i, --input`- Input Markdown file path
* `-o, --output` - Output file path
* `-f, --format` - Output format: `tex` or `html`
* `--keep-temp` - Keep temporary files for debugging (HTML only)
* `-h, --help` - Show help

## Syntax

- **Inline formulas:** `[$ ... ]`
- **Block formulas:** `$$ ... $$`
- **Block formulas with label:** `$$ ... $$(label)`
- **Reference to formula:** `[@label]` (displays as formula number)

## License

BUSL-1.1