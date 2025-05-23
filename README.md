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


