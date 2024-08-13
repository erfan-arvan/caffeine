#README

This directory contains Python scripts used to run the following 18 tools on the NJR-1 dataset.

## Tools

The tool-set includes 13 static analysis tools, 2 dynamic analysis tools, and 3
decompilers for Java.

#### Static Analysis Tools

1.  SpotBugs
2.  Wala
3.  Doop
4.  Soot
5.  Petablox
6.  Infer
7.  Error-Prone
8.  Checker-Framework
9.  Opium (Opal-framework)
10. Spoon
11. PMD
12. CheckStyle
13. JReduce

#### Dynamic Analysis tools (Profilers)

14. Wiretap
15. Jacoco

#### Decompilers

16. Procyon
17. CFR
18. Fernflower


## Dataset
The NJR-1 dataset consists of 293 Java programs. Each of these 12 tools run successfully on the entire dataset.
Additionally, each of these programs executes at least 100 unique application methods at runtime. 
These programs are repositories picked from the set of Java-8 projects on Github that compile and run successfully. 
Each of these programs come with a jar file, the compiled bytecode files, compiled library files
and the Java source code. 
The benchmark_stats.csv file uploaded along with this lists, for each benchmark, the number of nodes and edges in its dynamic application call-graph, as well as the number of edges in its static application call-graph (as computed by Wala). 
A summary of the same is listed here:

Statistics  Dynamic-Nodes  Dynamic-Edges  Static-Edges
Mean          205               469           1404
St.Dev        199               464           2523
Median        149               327           610

Each benchmark folder is organized as follows:

benchmark_name
|-> src
|-> classes
|-> jarfile
  |-> <jar_name>.jar
|-> lib
|-> info
  |-> mainclassname
  |-> classes
  |-> sources
  |-> declarations

'src' contains the Java source files, 'classes' contains the application's compiled Java bytecode files, 'lib' contains library classes needed to run the program. 'jarfile/<jar_name>.jar' is executable jar of the program, and the recommended way to execute the program or run an analysis on it is to use this jar file.In the 'info' folder, 'mainclassname' is a file containing the name of the class with the main() function, 'classes' is a list of application classes, 'sources' is a list of application source files, and 'declarations' is the set of method declarations. This information is typically required by whole-program static analysis tools. 
Notes:
-all programs use Java8, and it is advised to keep this as the default java version while running them.
-all classes found by a static-analysis tool which are not in the 'info/classes' file are either standard library or third-party library classes.
-the list of third-party library classes can be found by exploring the 'lib' folder.

## OVERVIEW OF SCRIPTS

The Python (Python3) scripts named run_<toolname>.py are used to run the tools.
Each script loops through the set of benchmarks and uses the 'os.system()' command to invoke the tool on a benchmark.
Ensure that the 'timeout' program is installed, since the scripts use it to time-out programs that take too long.
The scripts use the default options and configurations, and the script should be modified if a different functionality is desired.
If you wish to run another tool that is not mentioned here, you can create your own script along the lines of the ones available.
Of the tools listed here, SpotBugs, Wala, Doop, Soot, Petablox, Opium, JReduce, Wiretap, Jacoco, Procyon, CFR and Fernflower are run on the jar file of each benchmark.
Infer, ErrorProne, Checker-Framework, Spoon, PMD and Checkstyle run on the source code itself.

For each of the scripts, the following options need to be set at the 
top of the script.
a) benchmarks_folder: set the location to the dataset folder you downloaded.
b) results_folder: folder to store the analysis results.
c) skip_completed: set this option to true if you want to skip benchmarks whose outputs are already present in 'results_folder'
d) timeout: set the time limit for an analysis to run on a program. If the timeout expires, the benchmark is skipped..
e) timout_command: string representing the timeout command.
f) file_with_main_class: Just leave the default value. This is the location in each benchmark where the class of the program with the main() function is listed.

All the scripts are simply run with 'python run_<toolname>.py', to run the tool on all the benchmark.



#### SPOTBUGS

Download SpotBugs from the following link. The same link also has documentation.
https://spotbugs.readthedocs.io/en/stable/installing.html
Place the downloaded SpotBugs folder in the 'tools/spotbugs/' folder

As of uploading, SpotBugs v4.0.3 was tested. 
The following aditional constants should be correctly set in the 'run_spotbugs.py' file.
a) spotbugs_executable: set the location of the 'spotbugs' executable
b) spotbugs_command: set the analysis command and options

#### WALA
Download the Wala jars from the following link. 
https://mvnrepository.com/artifact/com.ibm.wala/com.ibm.wala.util/1.5.5
https://mvnrepository.com/artifact/com.ibm.wala/com.ibm.wala.shrike/1.5.5
https://mvnrepository.com/artifact/com.ibm.wala/com.ibm.wala.core/1.5.5

Place the downloaded jars in the 'tools/wala/' folder.
Wala documentation is available at 
http://wala.sourceforge.net/wiki/index.php/Main_Page

As of uploading Wala v1.5.5 was tested.
The following additional constants should be correctly set in the 'run_wala.py' file.
a) wala_core_jar: location of the downloaded core jar
b) wala_shrike_jar: location of the downloaded shrike jar
c) wala_util_jar: location of the downloaded util jar
d) driver_program: Since Wala is a library and not a stand-alone tool, we provide a sample driver program to list the reachable methods. This file can be modified as necesary.


#### DOOP
Install Souffle from this link (is a dependency for Doop).
https://souffle-lang.github.io/install (install souffle)

Then download Doop from this following link. 
https://bitbucket.org/yanniss/doop/downloads/?tab=tags 

Place the downloaded doop files in 'tools/doop/'.
Doop documentation is avaialable at
https://bitbucket.org/yanniss/doop/src/master/

As of uploading Doop v4.20.8 was tested. Note that an internet connection is needed to run Doop.
The following additional options need to be set:
a) doop_folder: Add the locatio of the downloaded doop folder.
b) doop_temp_result: Doop stores its result in the 'last-analysis' folder inside the Doop folder. Point to the output file you would like stored in the 'results_folder'. Set to 'Reachable.csv' by default.
c) doop_command: the command string to run Doop. Also includes the kind of analysis to be used and any other command line options to be used.

#### SOOT
Install Soot from the following link. Download the sootclasses-trunk-jar-with-dependencies.jar file
https://soot-build.cs.uni-paderborn.de/public/origin/master/soot/soot-master/4.1.0/build/

Place the downloaded jar in the 'tools/soot/' folder.
Soot documentation is available at
https://github.com/soot-oss/soot/wiki

As of uploading Soot v4.1.0  was tested.
The following additional options need to be set:
a) soot_jar: set the location of the downloaded Soot jar
b) driver_program: Since Soot is used a library, it needs a driver program. We provide a sample one at 'tools/soot/driver/SootCallgraph.java' which outputs the callgraph. It can be modified as necessary.
c) soot_options: the set of command line arguments to give soot.


#### PETABLOX
Download Petablox from the following link
https://github.com/petablox/petablox/releases
(Download petablox.jar from version 1.0)

Place the downloaded jar in 'tools/petablox/'
Petablox documentation is available at
https://github.com/petablox/petablox/wiki

As of uploading Petablox v1.0 was tested.
The following additional options need to be set:
a) petablox_jar: location of the downloaded jar 
b) petablox_options: the command line options to provide Petablox. By default it runs the 0CFA analysis
c) petablox_main_class: the main class that will be called from the Petablox jar. Leave as the default value.
d) petablox_temp_result: location of the temporary output that petablox produces. This file will be copied into the results folder.


#### INFER
Download the Infer binary from the following link
https://github.com/facebook/infer/releases/tag/v1.0.0

Place the downloaded jar in 'tools/infer/'
Infer documentation is available at
https://fbinfer.com/docs/getting-started

As of uploading Infer v1.0.0 was tested.
The following additional options need to be set:
a) compiled_classes_folder: a temporary folder to store class files compiled when infer was running
b) infer_binary: location of the infer binary
c) progress_bar_option: Infer typically shows a progress bar, which is inconvenient when running a script.
d) src_files: a temporary folder to store the source files.

#### ERROR-PRONE

Download the ErrorProne jars from the instructions listed u
Here is a list of them:
https://repo1.maven.org/maven2/com/google/errorprone/error_prone_core/2.5.1/error_prone_core-2.5.1-with-dependencies.jar
https://repo1.maven.org/maven2/org/checkerframework/dataflow-shaded/3.7.1/dataflow-shaded-3.7.1.jar
https://repo1.maven.org/maven2/com/google/code/findbugs/jFormatString/3.0.0/jFormatString-3.0.0.jar
https://repo1.maven.org/maven2/com/google/errorprone/javac/9+181-r4173-1/javac-9+181-r4173-1.jar

Place the downloaded jar in 'tools/errorprone/'
Documentation is available at
http://errorprone.info/docs/installation

The following additional options need to be set:
a) compiled_classes_folder: a temporary folder to store class files compiled wh
en infer was running
b) errorprone_dir: directory with the jar files
c) errorprone_jars: locations of the jar files, as you would write in a classpath command.
d) errorprone_command: the actual command. It is copied as is from the docs.


#### CHECKER-FRAMEWORK

Download the Checker-Framework distribution at 
https://checkerframework.org/checker-framework-3.11.0.zip

Place the downloaded folder in 'tools/checker-framework/'
Documentation is available at
https://checkerframework.org

As of uploading Checker-Framework v3.11.0 was tested.
The following additional options need to be set:
a) compiled_classes_folder: a temporary folder to store class files compiled when infer was running
b) cf_binary: location of the checker-framework binary
c) cf_command: the actual options for ruuning checker-framework. Must include the processor(i.e. checker) being used.

#### OPIUM
Opium is a tool in the Opal-Framework(https://www.opal-project.de).
Download 'opium.jar' from 
https://bitbucket.org/delors/opal/downloads/

Place the jar in tools/opium
Documentation is available at 
https://www.opal-project.de/Opium.html

As of uploading the Opium jar upload in July-2018 was tested.
The following additional options need to be set:
a) opium_jar: location of the jar


#### SPOON

Download the Spoon framework at 
https://mvnrepository.com/artifact/fr.inria.gforge.spoon/spoon-core
with all the dependencies.

Place the jar at tools/spoon
Documentation at https://spoon.gforge.inria.fr

As of uploading the Spoon v9.0.0 was tested.
The following additional options need to be set:
a) spoon_jar: location of the downloaded jar file.
b) spoon_folder: location of the spoon folder in tools
c) spoon_launcher: this is the main-class that will be called on the spoon jar
d) processor_name: Since Spoon is a library and not a stand-alone tool, we provide a sample driver program to list the set of empty catch blocks. This file can be modified as necesary. It needs to be located in the Spoon folder

#### PMD
 
Download PMD at 
https://pmd.github.io

Documentation at
https://pmd.github.io

As of uploading the PMD v6.32.0 was tested.
The following additional options need to be set:
a) compiled_classes_folder: a temporary folder to store class files compiled when infer was running
b) pmd_binary: location of the pmd binary
c) pmd_ruleset_option: the option which specifies the set of rules to be checked.


#### CHECKSTYLE

Download Checkstyle at
https://github.com/checkstyle/checkstyle/releases/

Documentation at
https://checkstyle.org

As of uploading, Checkstyle v8.41 was tested.
The following additional options need to be set.
a) cs_binary: location of the checkstyle binary
b) cs_options: the option which specifies the set of checks to be performed


#### JREDUCE

Install JReduce from here
https://github.com/ucla-pls/jreduce

Documentation is available at the same link

As of uploading, the Github commit 31c7a76a37a1830fac213e5626065203d68ba0ed on the master branch was tested.
The following additional options need to be set.
a) jre: location of the jre on your computer
b) jreduce_options: the options to be set (as specified on the github page)

#### WIRETAP

Install Wiretap from here
https://github.com/ucla-pls/wiretap

Documentation is available at the same link

As of uploading, the Github commit 90f4c3c32860cac7a5e48c0c2099ccec6dfaee7c on the master branch was tested.
The following additional options need to be set.
a) wiretap_jar: location of the built wiretap jar
b) wiretap_option: the options to be set

#### JACOCO

Download Jacoco at 
https://www.eclemma.org/jacoco/

Documentation at
https://www.jacoco.org/jacoco/trunk/doc/

As of uploading, Jacoco v0.8.7 was tested.
The following additional options need to be set.
a) jacoco_jar


#### PROCYON

Download Procyon at
https://bitbucket.org/mstrobel/procyon/downloads/

Documentation at
https://github.com/mstrobel/procyon/wiki/Java-Decompiler

As of uploading, the latest version on Oct 21,2021 was tested.
The following additional options need to be set.
a) procyon_jar


#### CFR

Download CFR at
https://github.com/leibnitz27/cfr/releases

Documentation at
https://www.benf.org/other/cfr/

As of uploading, CFR v0.151 was tested.
The following additional options need to be set.
a) cfr_jar

#### FERNFLOWER

Download Fernflower here and build using Gradle.
https://github.com/fesh0r/fernflower

Documentation is available at the same site.

As of uploading, Fernflower commit 133642f6302d2de05003b0dd9159a1e58ccd05c8 on the master branch was tested.
The following additional options need to be set.
a) fernflower_jar

