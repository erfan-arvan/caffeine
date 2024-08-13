/*
 * MIT License
 *
 * Copyright (c) 2024 Nima Karimipour
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package locator;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import javax.lang.model.element.ElementKind;

@AutoService(BugChecker.class)
@BugPattern(
    name = "AnnotLocator",
    altNames = {"NullableAnnotationLocator"},
    summary = "Locates existing Nullable annotations.",
    tags = BugPattern.StandardTags.STYLE,
    severity = SUGGESTION)
@SuppressWarnings("BugPatternNaming")
public class AnnotLocator extends BugChecker
    implements BugChecker.MethodTreeMatcher, BugChecker.VariableTreeMatcher {

  private static final Serializer serializer = new Serializer();

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    Symbol.MethodSymbol methodSymbol = ASTHelpers.getSymbol(tree);
    serializeSymIfNullable(methodSymbol);
    return Description.NO_MATCH;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    serializeSymIfNullable(ASTHelpers.getSymbol(tree));
    return Description.NO_MATCH;
  }

  /**
   * Serializes the symbol if annotated with explicit {@code @Nullable} annotations.
   *
   * @param symbol Given symbol to check its annotations.
   */
  private void serializeSymIfNullable(Symbol symbol) {
    if (symbol instanceof Symbol.MethodSymbol) {
      Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) symbol;
      for (int i = 0; i < methodSymbol.getParameters().size(); i++) {
        if (SymbolUtil.paramHasNullableAnnotation(methodSymbol, i)) {
          serializer.serializeNullableSymbol(methodSymbol.getParameters().get(i));
        }
      }
    }
    if ((symbol.getKind().equals(ElementKind.METHOD) || symbol.getKind().isField())
        && SymbolUtil.hasNullableAnnotation(symbol)) {
      serializer.serializeNullableSymbol(symbol);
    }
  }
}
