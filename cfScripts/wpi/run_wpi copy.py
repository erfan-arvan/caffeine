import os
import shutil
import time
import subprocess
import shlex
import zipfile

BENCHMARKS_FOLDER = "../dataset"
RESULTS_FOLDER = "checkerframework_3.34.0_results_WPI"
COMPILED_CLASSES_FOLDER = "cf_classes"
SRC_FILES = "cf_srcs.txt"
CF_BINARY = "../scripts/tools/checker-framework-3.34.0/checker/bin/javac"
CF_COMMAND = "-processor org.checkerframework.checker.nullness.NullnessChecker -Adetailedmsgtext -AassumePure"
SKIP_COMPLETED = True  # skips if the output file is already there.
WPI_TIMEOUT = 6 * 60 * 60  # 6 * 60 minutes

# Create the output folder if it doesn't exist
if not os.path.exists(RESULTS_FOLDER):
    os.mkdir(RESULTS_FOLDER)

# Function to unzip all JAR files into a single directory
def unzip_jars(lib_folder, extract_to):
    if os.path.exists(extract_to):
        shutil.rmtree(extract_to)
    os.makedirs(extract_to)
    jar_files = [os.path.join(lib_folder, f) for f in os.listdir(lib_folder) if f.endswith('.jar')]
    for jar_file in jar_files:
        with zipfile.ZipFile(jar_file, 'r') as jar:
            jar.extractall(extract_to)

# Loop through the benchmarks
print("Completed Benchmarks")
i, total = 1, len(os.listdir(BENCHMARKS_FOLDER))
for benchmark in os.listdir(BENCHMARKS_FOLDER):
    print(f"{i} of {total}: {benchmark}")
    if SKIP_COMPLETED:
        if os.path.exists(f'{RESULTS_FOLDER}/{benchmark}-before-wpi.txt'):
            print("skipping completed benchmark.")
            i += 1
            continue
        else:
            print("running benchmark.")
    # Skip non-directories
    if not os.path.isdir(f'{BENCHMARKS_FOLDER}/{benchmark}'):
        continue
    
    # Create a folder for the compiled classes if it doesn't exist
    if not os.path.exists(COMPILED_CLASSES_FOLDER):
        os.mkdir(COMPILED_CLASSES_FOLDER)

    # Get a list of Java source code files
    find_srcs_command = f'find {BENCHMARKS_FOLDER}/{benchmark}/src -name "*.java" > {SRC_FILES}'
    os.system(find_srcs_command)

    # Get folder with libraries used by benchmark
    lib_folder = f'{BENCHMARKS_FOLDER}/{benchmark}/lib'
    classpath_dir = os.path.join(BENCHMARKS_FOLDER, benchmark, 'classpath')
    unzip_jars(lib_folder, classpath_dir)

    command = (
        f'{CF_BINARY} '
        f'{CF_COMMAND} '
        '-AprintAllQualifiers -Averbosity=3 '
        '-J-Xmx32G '
        '-Xmaxerrs 10000 '
        '-d '
        f'{COMPILED_CLASSES_FOLDER} '
        f'-cp "{lib_folder}" '
        f'@{SRC_FILES} '
        f'2> {RESULTS_FOLDER}/{benchmark}-before-wpi.txt'
    )
    print(command)
    os.system(command)

    # Execute infer on the source files
    time_start = time.time()
    # Run WPI.
    print(f"Running WPI on {benchmark}")
    with open(f'{BENCHMARKS_FOLDER}/{benchmark}/wpi-log.txt', 'w') as file:
        process = subprocess.Popen(shlex.split(f'./wpi/wpi.sh {BENCHMARKS_FOLDER}/{benchmark}'), stdout=file, stderr=subprocess.STDOUT)
        try:
            process.communicate(timeout=WPI_TIMEOUT)
        except subprocess.TimeoutExpired:
            print(f"Command timed out after {WPI_TIMEOUT} seconds")
            process.kill()
            process.wait()
            os.system("killall -9 java")
    
    command = (
        f'{CF_BINARY} '
        f'{CF_COMMAND} '
        f'-Aajava={BENCHMARKS_FOLDER}/{benchmark}/wpi-out '
        '-J-Xmx32G '
        '-Xmaxerrs 10000 '
        '-d '
        f'{COMPILED_CLASSES_FOLDER} '
        f'-cp "{lib_folder}" '
        f'@{SRC_FILES} '
        f'2> {RESULTS_FOLDER}/{benchmark}-after-wpi.txt'
    )
    print(command)
    os.system(command)
    time_end = time.time()
    total_time = time_end - time_start
    with open("checkerframework_timings.csv", "a") as f:
        f.write(f'{benchmark},{str(total_time)}\n')
    
    # Remove the classes folder
    shutil.rmtree(COMPILED_CLASSES_FOLDER)
    shutil.rmtree(classpath_dir)
    print("-------------------------------------------------- \n")
