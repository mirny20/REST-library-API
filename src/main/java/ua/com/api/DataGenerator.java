package ua.com.api;

import com.github.javafaker.Faker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DataGenerator {
    private final static String fileName = "fillDB.sql";
    private final static String fileLocation = "src/main/resources";

    private final static int authorsCount = 150; // 9999 - is maximum (if set to max it greatly increase generation time)
    private final static int genresCount = 30;   // 30 is maximum; if set more, will work endlessly!!!
    private final static int booksCount = 1000;  // 9999 - is maximum (if set to max it greatly increase generation time)
    private static final Faker f = new Faker();

    private final static String insertAuthor = "insert into author (first_name,last_name,full_name,birth_city,birth_country,birth_date,nationality,description) values ('%s','%s','%s','%s','%s','%s','%s','%s');";
    private final static String insertGenre = "insert into genre (genre_name,genre_description) values ('%s','%s');";
    private final static String insertBook = "insert into book (book_name,book_description,book_language,pages_count,book_height,book_width,book_length,publication_year,author_id,genre_id) values ('%s','%s','%s',%d,%s,%s,%s,%d,%d,%d);";

    private static String escape(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    private static List<String> createAuthors() {
        String[] nationalities = {"Albanian", "American", "Australian", "Austrian", "Belgian", "British", "Bulgarian",
                "Canadian", "Chinese", "Czech", "Dutch", "Egyptian", "French", "German", "Greek", "Indian", "Irish",
                "Lithuanian", "Malaysian", "Mexican", "Moldovan", "New Zealander", "Romanian", "Scottish", "Spanish",
                "Swedish", "Turkish", "Ukrainian", "Welsh", "Syrian", "Slovenian", "Slovakian", "Polish", "Peruvian",
                "Namibian", "Nepalese", "Afghan", "Andorran", "Angolan", "Armenian", "Bahamian", "Cambodian",
                "Central African", "Colombian", "Cuban", "Equatorial Guinean", "Icelander", "Indonesian",
                "Kittian and Nevisian", "Liechtensteiner", "Lithuanian", "Luxembourger", "Maldivan", "Mongolian"};

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date from;
        Date to;

        try {
            from = formatter.parse("1920-01-01");
            to = formatter.parse("1999-12-31");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return IntStream.range(0, authorsCount)
                .mapToObj(i -> {
                    String firstName = f.name().firstName();
                    String lastName = f.name().lastName();
                    return String.format(insertAuthor,
                            escape(firstName),
                            escape(lastName),
                            escape(firstName + " " + lastName),
                            escape(f.address().city()),
                            escape(f.address().country()),
                            formatter.format(f.date().between(from, to)),
                            escape(nationalities[new Random().nextInt(nationalities.length)]),
                            escape(f.lorem().paragraph()));
                }).collect(Collectors.toList());
    }

    private static List<String> createGenres() {
        List<String> genreNames = new ArrayList<>();
        while (genreNames.size() < genresCount) {
            String name = f.book().genre();
            if (!genreNames.contains(name)) genreNames.add(name);
        }

        return IntStream.range(0, genresCount)
                .mapToObj(i -> String.format(insertGenre,
                        escape(genreNames.get(i)),
                        escape(f.lorem().paragraph())))
                .collect(Collectors.toList());
    }

    private static List<String> createBooks() {
        String[] languages = {"ukrainian", "german", "polish", "spanish", "chinese", "english", "portuguese",
                "croatian", "french", "arabic", "armenian", "urdu", "farsi", "japanese", "turkish", "georgian"};
        Supplier<String> bookName = () -> {
            List<String> words = new ArrayList<>();
            IntStream.rangeClosed(0, f.number().numberBetween(2, 5))
                    .forEach(i -> words.add(f.lorem().word()));
            Collections.shuffle(words);
            String first = words.get(0);
            words.set(0, first.substring(0, 1).toUpperCase() + first.substring(1));
            return String.join(" ", words);
        };

        return IntStream.range(0, booksCount)
                .mapToObj(i -> String.format(insertBook,
                        escape(bookName.get()),
                        escape(f.lorem().paragraph()),
                        escape(languages[new Random().nextInt(languages.length)]),
                        f.number().numberBetween(100, 1000),
                        String.valueOf(f.number().randomDouble(1, 5, 40)),
                        String.valueOf(f.number().randomDouble(1, 1, 5)),
                        String.valueOf(f.number().randomDouble(1, 5, 40)),
                        f.number().numberBetween(1970, Year.now().getValue()),
                        new Random().nextInt(authorsCount) + 1,
                        new Random().nextInt(genresCount) + 1))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<String> inserts = Stream.of(createAuthors(), createGenres(), createBooks())
                .peek(l -> System.out.println(l.size()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        File script = new File(Path.of(fileLocation, fileName).toString());
        try {
            if (script.getParentFile() != null) {
                script.getParentFile().mkdirs();
            }
            script.createNewFile();
            Files.write(script.toPath(), inserts);
            System.out.println("File " + fileName + " created in: " + fileLocation + "!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
