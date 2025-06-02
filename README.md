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
<div dir="rtl">

## پیاده‌سازی الگوی Strategy

### مروری کلی

در این پروژه، از **الگوی Strategy** برای بازآرایی عملیات معنایی در کلاس `CodeGenerator` استفاده شده است. این کار موجب کاهش پیچیدگی ساختار switch و تسهیل افزودن عملیات‌های جدید شده است.

---

### مراحل پیاده‌سازی

۱. تعریف واسط `SemanticStrategy`:

```java
public interface SemanticStrategy {
    void execute(Token next);
}
```

۲. ایجاد کلاس `CodeGeneratorContext` برای به‌اشتراک‌گذاری وضعیت بین استراتژی‌ها:

```java
public class CodeGeneratorContext {
    private final Memory memory;
    private final Stack<Address> ss;
    private final SymbolTable symbolTable;

    public CodeGeneratorContext(Memory memory, Stack<Address> ss, SymbolTable symbolTable) {
        this.memory = memory;
        this.ss = ss;
        this.symbolTable = symbolTable;
    }

    public Memory getMemory() { return memory; }
    public Stack<Address> getSs() { return ss; }
    public SymbolTable getSymbolTable() { return symbolTable; }
}
```

۳. پیاده‌سازی اولین استراتژی `AddStrategy`:

```java
public class AddStrategy implements SemanticStrategy {
    private final CodeGeneratorContext context;

    public AddStrategy(CodeGeneratorContext context) {
        this.context = context;
    }

    @Override
    public void execute(Token next) {
        Address temp = new Address(context.getMemory().getTemp(), varType.Int);
        Address s2 = context.getSs().pop();
        Address s1 = context.getSs().pop();

        if (s1.varType != varType.Int || s2.varType != varType.Int) {
            ErrorHandler.printError("In add two operands must be integer");
        }

        context.getMemory().add3AddressCode(Operation.ADD, s1, s2, temp);
        context.getSs().push(temp);
    }
}
```

۴. به‌روزرسانی کلاس `CodeGenerator` برای پشتیبانی از استراتژی‌ها:

```java
public class CodeGenerator {
    private Map<Integer, SemanticStrategy> strategies;
    private final CodeGeneratorContext context;

    public CodeGenerator(CodeGeneratorContext context) {
        this.context = context;
        initializeStrategies();
    }

    private void initializeStrategies() {
        strategies = new HashMap<>();
        strategies.put(10, new AddStrategy(context));
    }

    public void semanticFunction(int func, Token next) {
        SemanticStrategy strategy = strategies.get(func);
        if (strategy != null) {
            strategy.execute(next);
        } else {
            // fallback به switch-case برای عملیات‌هایی که هنوز تبدیل نشده‌اند
        }
    }
}
```

---

### مزایای استفاده از الگوی Strategy

- سازمان‌دهی بهتر کد و جداسازی عملیات‌ها
- افزودن آسان استراتژی‌های جدید بدون تغییر در کلاس اصلی
- کاهش وابستگی و پیچیدگی کلاس `CodeGenerator`
- افزایش قابلیت تست‌پذیری اجزا
- آماده‌سازی زیرساخت برای استفاده از الگوهای بیشتر مانند Factory
</div>
---
<div dir="rtl">

## بازآرایی Separate Query From Modifier

### مروری کلی

در این بازآرایی، تغییراتی در کلاس `Memory` اعمال شد تا عملیات‌های **پرس‌وجو (Query)** از **عملیات‌های تغییر وضعیت (Modifier)** جدا شوند. هدف از این بازطراحی، بهبود خوانایی، نگهداری‌پذیری و تست‌پذیری کد با جداسازی دقیق متدهایی بود که صرفاً مقدار بازمی‌گردانند از متدهایی که وضعیت را تغییر می‌دهند.

---

### تغییرات اعمال‌شده

#### ۱. افزودن متدهای پرس‌وجو (Query Methods)

```java
public int getCurrentTempIndex() {
    return lastTempIndex;
}

public int getCurrentDataAddress() {
    return lastDataAddress;
}
```

#### ۲. افزودن متدهای تغییر وضعیت (Modifier Methods)

```java
private void incrementTempIndex() {
    lastTempIndex += tempSize;
}

private void incrementDataAddress() {
    lastDataAddress += dataSize;
}
```

#### ۳. بازآرایی متدهای اصلی برای استفاده از روش تفکیکی

```java
public int getTemp() {
    int currentTemp = getCurrentTempIndex();
    incrementTempIndex();
    return currentTemp;
}

public int getDateAddress() {
    int currentData = getCurrentDataAddress();
    incrementDataAddress();
    return currentData;
}
```

---
### مزایای این بازآرایی

تفکیک بهتر مسئولیت‌ها: عملیات دریافت داده از عملیات تغییر وضعیت جدا شده‌اند. هر متد مسئولیت واضح و مشخصی دارد.

افزایش تست‌پذیری: متدهای Query و Modifier را می‌توان به‌صورت مستقل تست کرد و تغییرات وضعیت سیستم را به‌وضوح بررسی نمود.

نگهداری آسان‌تر: اگر نیاز به تغییر در نحوه خواندن یا به‌روزرسانی داده‌ها باشد، می‌توان به‌صورت جداگانه در هر بخش تغییر ایجاد کرد.

شفافیت بیشتر در هدف کد: نام‌گذاری متدها و ساختار فعلی کد به‌خوبی نشان می‌دهد که کدام متدها برای خواندن و کدام برای تغییر استفاده می‌شوند.

---
### جزئیات پیاده‌سازی

دو متد اصلی کلاس `Memory` شامل موارد زیر بازآرایی شدند:

1. `getTemp()`
2. `getDateAddress()`  


قبلاً مقدار temp را همزمان دریافت و به‌روزرسانی می‌کرد. اکنون ابتدا مقدار دریافت می‌شود، سپس جداگانه افزایش داده می‌شود.

همانند بالا، اکنون این متد نیز عملیات پرس‌وجو و به‌روزرسانی را از هم جدا کرده است.

</div>

---
<div dir="rtl">

## بازآرایی Replace Conditional with Polymorphism – استخراج عملگر تفریق

### مروری کلی

این مرحله ادامه‌ی پیاده‌سازی **الگوی Strategy** در کلاس `CodeGenerator` است. هدف، جایگزینی ساختار `switch` در متد `semanticFunction` با طراحی‌ای نگهداری‌پذیرتر و گسترش‌پذیرتر است. در این بخش، منطق مربوط به عملیات تفریق (`sub`) در یک کلاس استراتژی مستقل به نام `SubStrategy` استخراج شد.

---

### تغییرات اعمال‌شده

#### ۱. ایجاد کلاس `SubStrategy`

```java
public class SubStrategy implements SemanticStrategy {
    private final CodeGeneratorContext context;

    public SubStrategy(CodeGeneratorContext context) {
        this.context = context;
    }

    @Override
    public void execute(Token next) {
        Address temp = new Address(context.getMemory().getTemp(), varType.Int);
        Address s2 = context.getSs().pop();
        Address s1 = context.getSs().pop();

        if (s1.varType != varType.Int || s2.varType != varType.Int) {
            ErrorHandler.printError("In sub two operands must be integer");
        }
        context.getMemory().add3AddressCode(Operation.SUB, s1, s2, temp);
        context.getSs().push(temp);
    }
}
```

کلاس بالا منطق تفریق را پیاده‌سازی کرده و از `CodeGeneratorContext` برای دسترسی به پشته‌ی عملوندها (`ss`) و حافظه استفاده می‌کند.

---

#### ۲. به‌روزرسانی کلاس `CodeGenerator`

در کلاس `CodeGenerator`، تغییرات زیر اعمال شد:

- افزودن `SubStrategy` به لیست importها
- نمونه‌سازی و افزودن `SubStrategy` به نگاشت `strategies` با کلید 11 (عملیات `sub`)
- حذف `case 11` از ساختار `switch` در متد `semanticFunction`

```java
strategies.put(11, new SubStrategy(context)); // اضافه‌شدن SubStrategy
```

و حذف این بخش از switch:

```java
case 11:
    sub(); // حذف شده
    break;
```

---

### مزایای این بازآرایی

- **سازمان‌دهی بهتر کد:** منطق عملیات تفریق اکنون در یک کلاس مجزا قرار دارد و کلاس `CodeGenerator` ساده‌تر شده است.
- **نگهداری آسان‌تر:** تغییرات در منطق تفریق بدون تأثیر روی سایر بخش‌ها در کلاس `SubStrategy` قابل انجام است.
- **گسترش‌پذیری بالاتر:** افزودن عملیات‌های جدید فقط با ایجاد یک کلاس استراتژی جدید و اضافه‌کردن آن به map انجام می‌شود.
- **کاهش پیچیدگی:** ساختار `semanticFunction` سبک‌تر و قابل‌درک‌تر شده است.

---

### جزئیات پیاده‌سازی

- منطق عملیات `sub()` که شامل pop کردن عملوندها، بررسی نوع داده و تولید کد سه‌آدرسی بود، به متد `execute()` در `SubStrategy` منتقل شد.
- دسترسی به منابع اشتراکی مانند `Memory` و `ss` از طریق `CodeGeneratorContext` انجام می‌گیرد.

</div>
---
<div dir="rtl">

## بازآرایی Extract Method – جداسازی مدیریت خطا از `startParse`

### مروری کلی

در این بازنویسی، منطق مدیریت خطا در متد `startParse` از کلاس `Parser` جدا شده و به متدی مستقل به نام `handleParseError` منتقل شده است. این تغییر ساده ولی مؤثر باعث افزایش خوانایی و نگهداری‌پذیری کد می‌شود.

---

### تغییرات انجام‌شده

1. تعریف متد جدید `handleParseError` برای مدیریت خطاها
2. حذف کدهای کامنت‌شده و بلااستفاده
3. ساده‌سازی ساختار `try-catch` در متد اصلی

---

### مزایای این بازنویسی

- **خوانایی بهتر:** متد `startParse` اکنون ساده‌تر و قابل‌درک‌تر است.
- **جداسازی مسئولیت‌ها:** مدیریت خطا به‌صورت متمرکز و جداگانه انجام می‌شود.
- **نگهداری آسان‌تر:** افزودن تغییرات در منطق خطا به یک نقطه محدود شده است.
- **حذف کد مرده:** کدهای بلااستفاده که قبلاً کامنت شده بودند حذف شده‌اند.

---

### نمونه کد قبل از بازنویسی

```java
try {
    // ... کد اصلی ...
} catch (Exception ignored) {
    ignored.printStackTrace();
    // کدهای کامنت شده و غیرضروری
}
```

---

### نمونه کد بعد از بازنویسی

```java
try {
    // ... کد اصلی ...
} catch (Exception e) {
    handleParseError(e);
}

private void handleParseError(Exception e) {
    e.printStackTrace();
}
```

---

### نکات پیاده‌سازی

- متد `handleParseError` به‌صورت `private` تعریف شده است، زیرا فقط در درون کلاس استفاده می‌شود.
- نوع پارامتر این متد `Exception` است تا تمامی خطاها را بتوان مدیریت کرد.
- در حال حاضر، این متد تنها از `printStackTrace` استفاده می‌کند، اما قابلیت توسعه در آینده را دارد.


</div>

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
<div dir="rtl">

## شناسایی بوهای بد کد (Code Smells)
### 🔸 Long Method – متد طولانی  
**بخش کد:** متد `generatePhase2` در کلاس `Phase2CodeFileManipulator`  
**توضیح:** این متد بسیار طولانی است و وظایف متعددی را بر عهده دارد. بهتر است به متدهای کوچکتر تقسیم شود تا خوانایی و تست‌پذیری آن افزایش یابد.

### 🔸 Large Class – کلاس حجیم  
**بخش کد:** کلاس `Phase2CodeFileManipulator`  
**توضیح:** این کلاس شامل متدها و داده‌های بسیار متنوعی است و بهتر است با اعمال اصل Single Responsibility به چندین کلاس کوچک‌تر تقسیم شود.

### 🔸 Primitive Obsession – وسواس استفاده از انواع ابتدایی  
**بخش کد:** استفاده زیاد از `String` برای نگهداری نام کلاس‌ها در کلاس‌های `Phase2CodeFileManipulator` و `DiagramInfo`  
**توضیح:** بهتر است به‌جای `String`، از کلاس‌های اختصاصی استفاده شود تا صحت داده‌ها تضمین شود و از بروز خطا جلوگیری گردد.

### 🔸 Switch Statements – استفاده زیاد از `switch`  
**بخش کد:** متد `main` در کلاس `Main`  
**توضیح:** وجود دستورات متعدد `switch` برای مدیریت رفتارها می‌تواند با استفاده از Polymorphism یا الگوی Command بهینه‌سازی شود.

### 🔸 Feature Envy – وابستگی به ویژگی‌های کلاس دیگر  
**بخش کد:** متدهای `isHaveConstructor` و `isHaveDestructor` در کلاس `DiagramInfo`  
**توضیح:** این متدها بیش از حد به داده‌های کلاس `ClassInfo` وابسته‌اند. بهتر است به همان کلاس منتقل شوند.

### 🔸 Data Clumps – تکرار گروهی داده‌ها  
**بخش کد:** آرگومان‌های ورودی کلاس `Phase2CodeGenerator`  
**توضیح:** پارامترهایی مانند `diagramInfoDirectory` و `phase1Directory` به‌صورت تکراری با هم استفاده می‌شوند و باید در یک کلاس کمکی گروه‌بندی شوند.

### 🔸 Dead Code – کد مرده  
**بخش کد:** متد `generateInfoForXML` در کلاس `Main`  
**توضیح:** بخشی از کد مربوط به `DiagramInfo` کامنت شده و بدون استفاده است. باید حذف شود.

### 🔸 Long Parameter List – لیست طولانی پارامترها  
**بخش کد:** سازنده کلاس `Phase2CodeGenerator`  
**توضیح:** تعداد زیاد پارامترهای ورودی، استفاده از این کلاس را دشوار کرده است. باید پارامترها در قالب یک شیء انتقال داده شوند.


### 🔸 Message Chains – زنجیره پیام‌ها  
**بخش کد:** استفاده از `guiDiagram.getResultOfGraphOperation().getDependencyNumber()`  
**توضیح:** این زنجیره باعث افزایش وابستگی بین کلاس‌ها می‌شود. بهتر است یک متد میانجی در کلاس اصلی تعریف شود.


### 🔸 Overuse of Comments – استفاده بیش از حد از کامنت‌ها  
**بخش کد:** به‌ویژه در متد `generatePhase2`  
**توضیح:** وجود کامنت‌های زیاد به‌جای افزایش فهم کد، باعث شلوغی می‌شود. کد باید به شکلی نوشته شود که خودش گویا باشد.

</div>

### 6.در انتها بگویید پلاگین formatter چه می کند و چرا می تواند کمک کننده باشد و رابطه آن با باز آرایی کد چیست؟

به‌طور خودکار قواعد یکسان قالب‌بندی کد (مانند تورفتگی، فاصله‌گذاری، و شکستن خطوط) را در کل کد اعمال می‌کند. خوانایی کد را افزایش می‌دهد و از اتلاف وقت تیم‌ها بر سر اختلافات قالب‌بندی یا ناهماهنگی‌های ظاهری جلوگیری می‌کند. 

در حالی که بازآرایی ساختار کد را تغییر می‌دهد، Formatter کمک می‌کند تا کد در طول و بعد از این تغییرات، مرتب و دارای قالب‌بندی یکسان باقی بماند.


