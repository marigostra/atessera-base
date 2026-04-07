# Example Markdown Document

## Regular Text

This is regular text with *italic* and **bold** formatting. Here's some `inline code` and a [link](https://example.com).

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

## Mathematical Formulas

### Inline Formulas
Here's an inline formula: [$x_i] - x sub i.
Another one: [$E = mc^2] - Einstein's famous equation.
Complex inline: [$\sum_{i=1}^{n} i^2 = \frac{n(n+1)(2n+1)}{6}]

### Block Formulas
Simple equation:
$$ \frac{d}{dx} e^x = e^x $$

Equation with label (for referencing):
$$ \int_{0}^{\infty} e^{-x^2} dx = \frac{\sqrt{\pi}}{2} $$(gaussian-integral)

Matrix notation:
$$ \begin{pmatrix} a_{11} & a_{12} \\ a_{21} & a_{22} \end{pmatrix} \begin{pmatrix} x_1 \\ x_2 \end{pmatrix} = \begin{pmatrix} b_1 \\ b_2 \end{pmatrix} $$

### Advanced Math
$$ \Gamma(z) = \int_0^\infty t^{z-1}e^{-t}dt $$

$$ \zeta(s) = \sum_{n=1}^{\infty} \frac{1}{n^s} = \prod_{p \text{ prime}} \frac{1}{1-p^{-s}} $$

## Cross-References and Labels

You can reference formulas using [@gaussian-integral] to refer to the Gaussian integral formula.

## Lists

### Unordered List
* First item with formula [$x^2 + y^2 = r^2]
* Second item containing **bold text** and [$E = h\nu]
* Third item with nested list:
  * Nested item 1
  * Nested item 2 with [$e^{i\pi} + 1 = 0]

### Ordered List
1. Initialize parameters
2. Calculate using formula:
   $$ \hat{\theta} = \arg\max_{\theta} \mathcal{L}(\theta) $$
3. Validate results with [@gaussian-integral]
4. Document findings

### Mixed List
1. Step one: setup
   * Required packages
   * Configuration files
2. Step two: computation
   $$ \text{Result} = \int_a^b f(x)dx $$