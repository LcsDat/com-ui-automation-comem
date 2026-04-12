[33mcommit de7802ad4bfca145165ab247c850f763338be9a9[m[33m ([m[1;36mHEAD[m[33m -> [m[1;32mframework1[m[33m, [m[1;31morigin/framework1[m[33m)[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Sun Jun 15 22:19:40 2025 +0700

    Refactor login test cases 60%

[33mcommit 6c26c41e746284043023313b2931d8e64636095d[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Fri Jun 6 08:20:55 2025 +0700

    Write test case for Login function P4
    
    Continue refactoring

[33mcommit 652e23c4155ffb6f5db2b9b326de6a3755c98199[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Thu Jun 5 14:05:06 2025 +0700

    Write test case for Login function P3
    
    Complete a draft step and assertion for Login
    
    Next is refactoring for draft codes

[33mcommit 47306374cfca3fefb13c918de4a0caba6061f145[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Jun 3 16:01:31 2025 +0700

    Write test case for Login function P2
    
    Complete a draft step for Login same account in different tabs
    
    Complete a draft step for Login different account in different tabs

[33mcommit 331981f63493fc5f079c0f6542798790e127b616[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Jun 2 16:05:48 2025 +0700

    Resolve FB log in use the most stupid way: slow time input

[33mcommit 44dbdb505f9e180f091705b77fdabc472ea087f5[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon May 26 15:57:52 2025 +0700

    Write test cases for Login function P1

[33mcommit 3d43a0b748b17b37e50bf27cf48f6db3133d8a4f[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Sun May 25 14:23:55 2025 +0700

    add test cases file

[33mcommit efbd49b8f58fcc6f1b599a283504b24a5d910d21[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Fri May 23 15:11:29 2025 +0700

    Logging Refactor P3: fixed all logging problems
    
    Next: Configure CICD or data-driven testing

[33mcommit 3b16ad8ea295a06942d52c3091568a5a510b5024[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Thu May 22 16:24:07 2025 +0700

    Extent Refactor P2: refactored log4j2 -> separate into a new class that can use for whole project
    
    Now changing from Extent Refactor to Logging Refactor

[33mcommit 2a56f6c2900c183ac3356b20bb73a51d365b9926[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed May 21 23:46:00 2025 +0700

    Solved symbol does not display in console
    
    Next: Refactor logging format + move logger into a separated class

[33mcommit e55a29a9b092abd68e56d616d30a9ccc2a3279a3[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue May 20 23:32:09 2025 +0700

    Solved parallel testing using Extent report + log4j2
    
    Next: Refactor logging template

[33mcommit ec66c35bae1b48835925ee35756cf8018a5d9b71[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue May 20 18:39:55 2025 +0700

    resolved parallel testing + extent logging
    
    next need to resolve log4j2 logging and refactor log template

[33mcommit e3f531dac3b7519f94e80e4aa0fe90fac8bf7688[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon May 19 21:45:44 2025 +0700

    still solving extent report

[33mcommit f4d1a99ae2a47290e3167d8d7a0dd12ff4511b57[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon May 19 15:58:01 2025 +0700

    still not fixed

[33mcommit 59b1b4dc3e6be8f6d6ffbbb038f884a80bc9e95e[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Sun May 18 21:35:16 2025 +0700

    Extent Report Refactor P1
    
    Refactor to make, when run test parallely, log will be split into separated suite

[33mcommit f12fc0e4ed870756b10d4c9dce520439c22ee4f2[m
Merge: c849bd1 0346bfb
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed May 14 10:11:32 2025 +0700

    resolve conflict

[33mcommit c849bd1980943778823cefe848b9ef4a0d4cc97c[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed May 14 09:47:00 2025 +0700

    fail

[33mcommit 1b9c4d85f5b2bd10d9d02224ce0efd4942abf0e5[m
Merge: 882e4be 9e22e99
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed May 14 08:13:24 2025 +0700

    fail?

[33mcommit 0346bfbd40eb6b53afb81abfa55f6698c2da3dbf[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue May 13 16:32:36 2025 +0700

    Next problem: refactor before apply CICD

[33mcommit 3aafd995b8418c5803ed95822fcb52b1ac88b5f7[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue May 13 15:47:50 2025 +0700

    Resolved: Parallel + Headless testing + Extent test result is split per test suite

[33mcommit 9155ac41a7d42433a4c2922c57b1ba84339b9dd0[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon May 12 16:03:19 2025 +0700

    Extent Report P1: resolved null pointer to receive ExtentTest instance
    
    Next: need to find a way for parallel testing, that test result is separated by each test class

[33mcommit 882e4be5865a4d12795e44ce97facbaae621b50f[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Thu May 8 07:41:51 2025 +0700

    error?

[33mcommit 9e22e9935df12e90c7be0835a8afd240e80f89be[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed May 7 16:30:46 2025 +0700

    problem: Extent report null

[33mcommit e2dd47c8314ef6d2b58a7327ba8be14789be658e[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue May 6 16:36:41 2025 +0700

    Need to resolve headless testing in firefox

[33mcommit df0e1e973eb1c8f424415a71ee545fa3e3afd127[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon May 5 23:27:19 2025 +0700

    Completed to run test using maven command line via testng xml file

[33mcommit 1ac389fc2dec97dcb465a356962924e4f483608e[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon May 5 22:40:08 2025 +0700

    Practice run test by maven command line

[33mcommit b3728374a7dd5639a090c6efdf76396ba96fd834[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon May 5 16:28:54 2025 +0700

    Log4j2 P2: modified log color and icon

[33mcommit c8bc33963fe5dbfd5909f7f8e2285ea2a863d73c[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon May 5 15:28:21 2025 +0700

    Modified Extent Logging

[33mcommit a0d10b7439857642cc97d0cc9bd094b615e4f197[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Thu Apr 24 15:47:42 2025 +0700

    Log4j2 P1: Implement log4j2 as logging tool of the test framework
    
    Problem: need to deep dive Json Template Layout

[33mcommit 97ef6ce743ec3957ca0ef21fe8f943e605b736fc[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Thu Apr 24 11:17:32 2025 +0700

    Completed configuring Extent Report
    
    Modified verification console to easily view and understand

[33mcommit ce72dc5285fa48a1397257755abfa4c27e99e54c[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Apr 23 16:26:06 2025 +0700

    Report P1: Configure Extent Report V5 as the test report output

[33mcommit 0ad6ebd9d35201d63341c54455181d9798324cf9[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Apr 23 13:42:03 2025 +0700

    Expand E2E test case: completed
    
    Completed running multiple browsers

[33mcommit 8caeb1733f262a4b56ca0c90c16d456d4fd0685e[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 22 22:03:17 2025 +0700

    temp

[33mcommit 3690862d8d2906908d81753c5cf0aa3ee4de5ee8[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Apr 21 16:20:15 2025 +0700

    Expand E2E test case P2
    
    Payment method part

[33mcommit bed3cbc35adc3ab67ac4fb14ac204cb6b87ec09e[m[33m ([m[1;31morigin/laptop[m[33m, [m[1;32mlaptop[m[33m)[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Apr 21 14:47:29 2025 +0700

    Expand E2E test case: Comple flow to Payment Page
    
    Done for change address

[33mcommit 2666679e65e956ea85ab5c36d4eea7ac67b4d772[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Apr 21 06:19:12 2025 +0700

    quick push

[33mcommit ef6596a19a9817a89ad7d21be208ece03b1c54e7[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Apr 16 16:09:28 2025 +0700

    Refactor P6: Custom Assertion
    
    Completely edit text color when print in console. Apply printf and know some color code

[33mcommit e1b3a5a0461461f957e991873b16a5f0927d6c77[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Apr 16 12:47:15 2025 +0700

    Refactor P5
    
    Custom Assertion: A solution to avoid hard coding is naming unique package, make it show only useful info into console
    
    Plus: custom color when printing in console

[33mcommit e5b7b223b197e0d61cb3b432d2af0b15f059f1b0[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 15 17:42:03 2025 +0700

    Refactor P4: Custom Assertion
    
    Reason: To avoid TestNG hard assert terminating if got a fail assertion
    
    Custom Assert P1: Mostly hard coded, need to refactor code

[33mcommit b0d37b9a8485ff3dec78c029ebbba44d78fd80f7[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 15 11:43:17 2025 +0700

    Refactor code in some classes

[33mcommit aa647f426d3ba535f466ab0ed705330e163515e7[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Apr 14 17:49:09 2025 +0700

    refactor auto-detect locator strategy method from WebsiteElement class to WebsiteDriver class

[33mcommit a905f44860346ffc39504465334aafde459705bb[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Thu Apr 10 22:21:16 2025 +0700

    Refactor P3: completed handle hamber menu, one method choose directly category, other will choose product type of the category

[33mcommit 7981bcaeb8322a5422472cbf058c5d0f1d1d32b1[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Apr 9 16:29:14 2025 +0700

    Refactor P2: Handle hameber menu (Not complete)

[33mcommit 9c3406041eff216fea6875bba00321569765e0a5[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Apr 9 15:40:02 2025 +0700

    Refactor code P1:
    
    * Identify Homepage and ProductPage have common elements
    
    -> Define a class which is only extended by Homepage and ProductPage

[33mcommit df5987da2493aa9e8363951c4a855c9e4b97c1f5[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Apr 9 10:46:42 2025 +0700

    Implemented findElements method with varargs
    
    Implemented test cases for FAQ Page

[33mcommit 13acc14b044d38e5f3ea146d813a1208c6acff26[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 8 16:16:01 2025 +0700

    New test case in Homepage: navigate to Stores Location then verify
    
    New test teardown step for switch window test cases: switch to main website

[33mcommit 639d2ce9d541f2ee6f72df4060cb2d77df3f459c[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 8 14:51:39 2025 +0700

    Implemented switchWindow method

[33mcommit d92f0f1c6d51c556aef0c1b3a8f3a45aca0dc7fe[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Fri Apr 4 21:08:57 2025 +0700

    Write some points to refactor code
    
    think new test case to open another tab

[33mcommit d945f210de23b974ff6c98c2e5e61c5121f2896f[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Fri Apr 4 20:34:29 2025 +0700

    Redefine test setup: init driver, pass driver into pages -> remove popup and cookies -> if login required, remove all products in cart
    
    Test tear down: if logged in, log out -> clean driver process
    
    Implemented some common methods in BaseTest class

[33mcommit a18803de7eb1f0367804183d5e2e01dca460b07f[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Thu Apr 3 22:11:04 2025 +0700

    method checkQuantity has problems, need to fix

[33mcommit 07515bd6205284103e76b53631ce96a398f19245[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Apr 2 22:33:55 2025 +0700

    Need to research TestNG ro configure run paralelly
    
    :wq

[33mcommit 4dafaf3c90c1626caf15e947f8c60d29c83914b8[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 1 16:21:49 2025 +0700

    Split test cases into 2 class
    
    Implemented check element undisplayed

[33mcommit 1e398df27723d16a9d5f1b385f00a9849f20009f[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 1 14:07:42 2025 +0700

    Created BaseTest to store set up and tear down steps

[33mcommit afd1425c633e2a623d09a0a6362e733da19038c5[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 1 12:20:41 2025 +0700

    Write one more test case

[33mcommit 8a728d97f4b802d16ad3b125ad090edbafd39ac2[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Apr 1 10:54:08 2025 +0700

    Write one more test case

[33mcommit 49b4a043a99e9e478965d9279c890d9f47a871f8[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Mar 31 22:22:38 2025 +0700

    Fixed findElements method
    
    Solution: For the first implementation of findElements, I tried locator + [index]
    
    Instead of that, the correct way is (locator) + [index]

[33mcommit b928f92f9d90e998dc13e7a1580947698b49770c[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Mar 31 16:30:24 2025 +0700

    Problem: new method findElements is wrongly implemented. Need to find the reason

[33mcommit f924e818d1e28fef535e2e251d6f398ed6f1b12e[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Mar 31 14:56:49 2025 +0700

    Implemented method to kill driver process after each run

[33mcommit 9651d30a8f6b1813a8b527017a74f691e13c4c17[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Mon Mar 31 12:23:13 2025 +0700

    Refactored code in HomePageTest

[33mcommit 47b83897c9ec2566dd21dce1120eb5338df779df[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Sun Mar 30 21:57:26 2025 +0700

    implemented method findElements()
    
    Next is refactor code in HomePageTest using new method

[33mcommit fbc5cd57dd0dd554ae29e1be72b2fc638fe34bbc[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Sun Mar 30 12:49:56 2025 +0700

    fixed method find element: When init By.className(), the parameter must not include whitespace
    
    Solution: before passing the parameter into By.className(), remove all whitespaces

[33mcommit c47d5cd823cfc585c1f3a4f0f10fec19774e0489[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Fri Mar 28 21:25:35 2025 +0700

    implemented new classes 'WebsiteActions', 'ExplicitWait'
    
    Have to fix issue when using By.className strategy

[33mcommit 59a432ec32b5b06b60940abf85e462279a9af509[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Fri Mar 28 14:27:03 2025 +0700

    Created WebsiteElement class
    
    Adding a new method to find element, which only need pass the locator inside then auto detect the locator strategy

[33mcommit aab02cf968b55e0f14a588c3a571667c2393355f[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Thu Mar 27 11:09:41 2025 +0700

    applying POM p4
    
    implemented methods and locators in ProductPage and ProductDetailsPage classes

[33mcommit 2f2bfa5153c06e4231134c9115b916fbb6676d01[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Mar 26 16:17:14 2025 +0700

    starting apply POM
    
    apply var arguments to make locator dynamically

[33mcommit 8f3630f4fba41c358cafdeffb1593e20e2b9338b[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Mar 26 15:44:25 2025 +0700

    starting apply POM
    
    apply var arguments to make locator dynamically

[33mcommit ec309b01e039a774fc18a490a53c1725180a0687[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Mar 26 15:06:08 2025 +0700

    starting apply POM
    
    implemented methods and locators into Homepage

[33mcommit 2cc9830e160290f7f09ec8d7856307a30957a042[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Mar 26 12:41:27 2025 +0700

    split scenario into setup, tear down and execution steps

[33mcommit 94317b91873c7f1035075a0a4b0e8e7d09e0295d[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Wed Mar 26 10:01:07 2025 +0700

    new Actions method in WebsiteDriver class
    
    new explicit wait method in WebsiteDriver class
    
    refactor test scenario code by new class

[33mcommit 3219c7ff500c220eaf88dceebc9bc873be7bb01c[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Mar 25 16:21:35 2025 +0700

    Build Driver class to refactor code

[33mcommit 0b72234ce1c17cf37f5a1918d628ccec0a8959e6[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Mar 25 15:24:05 2025 +0700

    refactor init browser driver

[33mcommit b4e9b4e0321ceaa4fb11ca348020e33c964bf136[m[33m ([m[1;31morigin/main[m[33m, [m[1;32mmain[m[33m)[m
Author: LcsDat <datle.testing01@gmail.com>
Date:   Tue Mar 25 11:01:32 2025 +0700

    completed add product to cart scenario
