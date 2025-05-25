# Code-Refactoring
# MiniJava
Mini-Java is a subset of Java. MiniJava compiler implement a compiler for the Mini-java
programming language.

# Rules of MiniJava
```
Goal --> Source EOF
Source --> ClassDeclarations MainClass
MainClass --> class Identifier { public static void main() { VarDeclarations Statements}}
ClassDeclarations --> ClassDeclaration ClassDeclarations | lambda
ClassDeclaration --> class Identifier Extension { FieldDeclarations MethodDeclarations }
Extension --> extends Identifier | lambda
FieldDeclarations --> FieldDeclaration FieldDeclarations | lambda
FieldDeclaration --> static Type Identifier ;
VarDeclarations --> VarDeclaration VarDeclarations | lambda
VarDeclaration --> Type Identifier ;
MethodDeclarations --> MethodDeclaration MethodDeclarations | lambda
MethodDeclaration --> public static Type Identifier ( Parameters ) { VarDeclarations Statements return GenExpression ; }
Parameters --> Type Identifier Parameter | lambda
Parameter --> , Type Identifier Parameter | lambda
Type --> boolean | int
Statements --> Statements Statement | lambda
Statement --> { Statements } | if ( GenExpression ) Statement else Statement | while ( GenExpression ) Statement | System.out.println ( GenExpression ) ; | Identifier = GenExpression ;
GenExpression --> Expression | RelExpression
Expression --> Expression + Term | Expression - Term | Term
Term --> Term * Factor | Factor
Factor --> ( Expression ) | Identifier | Identifier . Identifier | Identifier . Identifier ( Arguments ) | true | false | Integer
RelExpression --> RelExpression && RelTerm | RelTerm
RelTerm --> Expression == Expression | Expression < Expression
Arguments --> GenExpression Argument | lambda
Argument --> , GenExpression Argument | lambda
Identifier --> <IDENTIFIER_LITERAL>
Integer --> <INTEGER_LITERAL>
```
# Refactoring

## Facade 1
از آنجا که کلاس Parser تنها از دو تابع CodeGenerator استفاده میکند، میتوان این دو تابع را بصورت یک واسط Facade به نام ParserCodeGeneratorFacade جدا کرد
یک واسط ساده‌شده برای زیرسیستم تولید کد فراهم می‌کند و وابستگی بین کلاس‌های Parser و CodeGenerator را کاهش می‌دهد.این کار باعث می‌شود کد قابل نگهداری‌تر شده و در آینده راحت‌تر قابل تغییر باشد.
```
package MiniJava.parser;

import MiniJava.codeGenerator.CodeGenerator;
import MiniJava.scanner.token.Token;

public class ParserCodeGeneratorFacade {
    private final CodeGenerator cg;

    public ParserCodeGeneratorFacade() {
        this.cg = new CodeGenerator();
    }

    public void semanticFunction(int func, Token next) {
        cg.semanticFunction(func, next);
    }

    public void printMemory() {
        cg.printMemory();
    }
}
```

---
## Facade 2
از آنجا که کلاس SymbolTable تنها از یک تابع Memory استفاده میکند، میتوان این تابع را بصورت یک واسط Facade به نام SymbolTableMemoryFacade جدا کرد.

```
package MiniJava.semantic.symbol;

import MiniJava.codeGenerator.Memory;

public class SymbolTableMemoryFacade {
    private Memory mem;

    public SymbolTableMemoryFacade(Memory mem) {
        this.mem = mem;
    }

    public int getDateAddress() {
        return mem.getDateAddress();
    }
}
```
---
## Self Encapsulate Field
این موضوع در چندین کلاس رعایت نشده است. یکی از نمونه‌‌های آن در کلاس Parser است که متغیر private زیر در داخل کلاس بصورت direct access استفاده شده‌ اند

```
private ArrayList<Rule> rules;
private Stack<Integer> parsStack;
private ParseTable parseTable;
private lexicalAnalyzer lexicalAnalyzer;
private ParserCodeGeneratorFacade cg;
```
برای اصلاح آن ،مطابق موارد زیر عمل می کنیم :

**How to Refactor**
- Create a getter (and optional setter) for the field. They should be either protected or public.
- Find all direct invocations of the field and replace them with getter and setter calls.

برای همین متد های زیر را به کلاس parser اضافه می کنیم و هر جا که این field  ها استفاده شده اند ، با متد مورد نظر جایگزین می کنیم و کلاس parser در نهایت به شکل زیر تغییر پیدا می کند:
```
package MiniJava.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Stack;

import MiniJava.Log.Log;
import MiniJava.codeGenerator.CodeGenerator;
import MiniJava.errorHandler.ErrorHandler;
import MiniJava.scanner.lexicalAnalyzer;
import MiniJava.scanner.token.Token;

public class Parser {
    private ArrayList<Rule> rules;
    private Stack<Integer> parsStack;
    private ParseTable parseTable;
    private lexicalAnalyzer lexicalAnalyzer;
    private ParserCodeGeneratorFacade cg;

    public Parser() {
        parsStack = new Stack<Integer>();
        pushState(0);
        try {
            parseTable = new ParseTable(Files.readAllLines(Paths.get("src/main/resources/parseTable")).get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        rules = new ArrayList<Rule>();
        try {
            for (String stringRule : Files.readAllLines(Paths.get("src/main/resources/Rules"))) {
                rules.add(new Rule(stringRule));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        cg = new ParserCodeGeneratorFacade();
    }

    public void startParse(java.util.Scanner sc) {
        lexicalAnalyzer = new lexicalAnalyzer(sc);
        Token lookAhead = lexicalAnalyzer.getNextToken();
        boolean finish = false;
        Action currentAction;
        while (!finish) {
            try {
                Log.print(lookAhead.toString() + "\t" + peekState());
                currentAction = parseTable.getActionTable(peekState(), lookAhead);
                Log.print(currentAction.toString());

                switch (currentAction.action) {
                    case shift:
                        pushState(currentAction.number);
                        lookAhead = lexicalAnalyzer.getNextToken();
                        break;

                    case reduce:
                        Rule rule = rules.get(currentAction.number);
                        for (int i = 0; i < rule.RHS.size(); i++) {
                            popState();
                        }

                        Log.print(peekState() + "\t" + rule.LHS);
                        pushState(parseTable.getGotoTable(peekState(), rule.LHS));
                        Log.print(peekState() + "");
                        try {
                            cg.semanticFunction(rule.semanticAction, lookAhead);
                        } catch (Exception e) {
                            Log.print("Code Genetator Error");
                        }
                        break;

                    case accept:
                        finish = true;
                        break;
                }
                Log.print("");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        if (!ErrorHandler.hasError) cg.printMemory();
    }

    // === Self-Encapsulated Field methods for parsStack ===
    private void pushState(int state) {
        parsStack.push(state);
    }

    private int popState() {
        return parsStack.pop();
    }

    private int peekState() {
        return parsStack.peek();
    }
}

```
---

# پاسخ سوالات 
### 1.هر یک از مفاهیم زیر را در حد یک خط توضیح دهید.

 - کد تمیز- Clean Code
کدی که خواندن، درک و ویرایش آن آسان باشد و هدف و منطق خود را به‌وضوح نشان دهد.

 - بدهی فنی- Technical Debt
هزینه‌ای که بابت به تعویق انداختن بهبود کد یا انتخاب راه‌حل ساده‌تر فعلی پرداخت می‌شود، که ممکن است در آینده مشکل‌ساز شود.

- بوی بد کد- Code Smell
نشانه‌ای در کد که ممکن است نشان‌دهندهٔ مشکل در طراحی یا ساختار باشد، حتی اگر فعلاً منجر به خطا نشده باشد.


  
### 2.طبق دسته‌بندی وب‌سایت refactoring.guru، بوهای بد کد به پنج دسته تقسیم می‌شوند. در مورد هر کدام از این پنج دسته توضیح مختصری دهید.

#### 1. بزرگ‌نماها-Bloaters
   کلاس‌ها یا متدهایی که بیش از حد بزرگ و پیچیده شده‌اند، به‌طوری که مدیریت و درک آن‌ها دشوار شده است.

#### 2. سوءاستفاده‌کنندگان از شی‌گرایی-Object-Orientation Abusers
  این دسته به بوی بدهایی اشاره دارد که از اصول شی‌گرایی به‌درستی استفاده نکرده‌اند، مانند وراثت نادرست یا استفادهٔ نابه‌جا از کلاس‌ها.

#### 3. موانع تغییر-Change Preventers
  بوهایی که باعث می‌شوند تغییر در یک بخش از کد نیازمند تغییرات متعدد در بخش‌های دیگر شود، و فرآیند نگهداری را سخت می‌کنند.

####  4. موارد زائد-Dispensables
  عناصر غیرضروری در کد—مانند کدهای مرده یا کامنت‌های اضافی—که می‌توان آن‌ها را حذف کرد تا کد ساده‌تر و شفاف‌تر شود.

#### 5. وابستگی‌های زیاد-Couplers
  بوهایی که نشان‌دهندهٔ وابستگی شدید بین کلاس‌ها هستند، که باعث شکننده شدن کد و سخت شدن نگهداری آن می‌شود.


### 3.	یکی از انواع بوهای بد، Feature Envy است. 
 - این بوی بد در کدام یک از دسته بندی های پنجگانه قرار می گیرد؟
در دستهٔ Couplers (وابستگی‌های زیاد) 
 - برای برطرف کردن این بو، استفاده از کدام بازآرایی ها پیشنهاد می شود؟ 
رایج‌ترین بازآرایی‌ها برای رفع این بو شامل انتقال متد (Move Method)، استخراج متد (Extract Method)، و ادغام متد (Inline Method) هستند، بسته به شرایط.
 - در چه مواقعی باید این بو را نادیده گرفت؟ 
رایج‌ترین بازآرایی‌ها برای رفع این بو شامل انتقال متد (Move Method)، استخراج متد (Extract Method)، و ادغام متد (Inline Method) هستند، بسته به شرایط.
### 4.	دو مورد از تفاوت های بین “Code Smell” و Bug”“ را بنویسید. 

- باگ باعث رفتار نادرست در زمان اجرا می‌شود، در حالی که بوی بد کد بیشتر به ساختار ضعیف یا دشواری در نگهداری اشاره دارد.
- رفع باگ برای عملکرد صحیح کد ضروری است، اما رفع بوی بد کد اختیاری بوده و با هدف بهبود کیفیت بلندمدت کد انجام می‌شود.

### 5.در وبسایت 29 بوی بد کد نامبرده شده است. سعی کنید 10 بوی بد را در پروژه تبدیل کننده مدل به سی پیدا کنید و به آن اشاره کنید.

### 6.در انتها بگویید پلاگین formatter چه می کند و چرا می تواند کمک کننده باشد و رابطه آن با باز آرایی کد چیست؟

به‌طور خودکار قواعد یکسان قالب‌بندی کد (مانند تورفتگی، فاصله‌گذاری، و شکستن خطوط) را در کل کد اعمال می‌کند. خوانایی کد را افزایش می‌دهد و از اتلاف وقت تیم‌ها بر سر اختلافات قالب‌بندی یا ناهماهنگی‌های ظاهری جلوگیری می‌کند. 

در حالی که بازآرایی ساختار کد را تغییر می‌دهد، Formatter کمک می‌کند تا کد در طول و بعد از این تغییرات، مرتب و دارای قالب‌بندی یکسان باقی بماند.


