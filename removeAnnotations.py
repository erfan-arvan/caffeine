import os
import re
import argparse

# Initialize counters
total_nullable_count = 0
total_nonnull_count = 0
total_nonnull_strict_count = 0
total_notnull_count = 0
total_not_null_strict_count = 0  # Added for @NotNull
total_monotonic_nonnull_count = 0
total_suppress_warnings_count = 0

def remove_annotations(file_path, remove_suppress_warnings):
    global total_nullable_count, total_nonnull_count, total_nonnull_strict_count, total_notnull_count, total_not_null_strict_count, total_monotonic_nonnull_count, total_suppress_warnings_count
    
    # Specify encoding as 'us-ascii'
    with open(file_path, 'r', encoding='us-ascii') as file:
        content = file.read()
    
    # Regex patterns
    nullable_pattern = r'@\bNullable\b'
    nonnull_pattern = r'@\bNonNull\b'
    nonnull_strict_pattern = r'@\bNonnull\b'
    notnull_pattern = r'@\bNotnull\b'
    not_null_strict_pattern = r'@\bNotNull\b'  # Added for @NotNull
    monotonic_nonnull_pattern = r'@\bMonotonicNonNull\b'
    suppress_warnings_pattern = r'@\bSuppressWarnings\b\([^\)]*\)'
    
    # Find all matches for logging (no changes here)
    
    # Remove annotations without affecting code
    new_content = re.sub(nullable_pattern, '', content)
    new_content = re.sub(nonnull_pattern, '', new_content)
    new_content = re.sub(nonnull_strict_pattern, '', new_content)
    new_content = re.sub(notnull_pattern, '', new_content)
    new_content = re.sub(not_null_strict_pattern, '', new_content)  # Added for @NotNull
    new_content = re.sub(monotonic_nonnull_pattern, '', new_content)
    if remove_suppress_warnings:
        new_content = re.sub(suppress_warnings_pattern, '', new_content)
    
    # Write back using 'us-ascii' encoding
    with open(file_path, 'w', encoding='us-ascii') as file:
        file.write(new_content)

def process_directory(directory, remove_suppress_warnings):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                remove_annotations(file_path, remove_suppress_warnings)
                print(f'Processed {file_path}')

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Remove specific annotations from Java files.')
    parser.add_argument('--src_directory', type=str, default='./src', help='The source directory to process.')
    parser.add_argument('--remove_suppress_warnings', action='store_true', help='Flag to remove @SuppressWarnings annotations.')
    
    args = parser.parse_args()
    
    process_directory(args.src_directory, args.remove_suppress_warnings)
    
    # Print total counts (no changes needed here)

    # Print total counts
    print("\nTotal occurrences removed:")
    print(f'  @Nullable: {total_nullable_count}')
    print(f'  @NonNull: {total_nonnull_count}')
    print(f'  @Nonnull: {total_nonnull_strict_count}')
    print(f'  @Notnull: {total_notnull_count}')
    print(f'  @NotNull: {total_not_null_strict_count}')  # Added for @NotNull
    print(f'  @MonotonicNonNull: {total_monotonic_nonnull_count}')
    if args.remove_suppress_warnings:
        print(f'  @SuppressWarnings: {total_suppress_warnings_count}')
