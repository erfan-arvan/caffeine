package remover;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

  public static void main(String[] args) throws Exception {
    Path root = Paths.get(args[0]);
    try (Stream<Path> paths = Files.walk(root)) {
      paths.forEach(
          path -> {
            if (path.toString().endsWith(".java")) {
              // read content
              try {
                String content = Files.readString(path);
                content = content.replaceAll("@Nullable", "");
                Files.write(path, content.getBytes());
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          });
    }
  }
}
