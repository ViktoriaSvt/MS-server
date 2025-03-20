//package skytales.Library.util;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.BulkRequest;
//import co.elastic.clients.elasticsearch.core.BulkResponse;
//import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
//import org.springframework.web.bind.annotation.*;
//import skytales.Library.service.BookService;
//import skytales.Library.web.dto.BookData;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("api/books")
//public class BulkInsert {
//
//    private final ElasticsearchClient elasticsearchClient;
//    private final BookService bookService;
//
//    public BulkInsert(ElasticsearchClient elasticsearchClient, BookService bookService) {
//        this.elasticsearchClient = elasticsearchClient;
//        this.bookService = bookService;
//    }
//
//    private static final String COVER_DIRECTORY = "C:/Covers";
//    private static final String BACKGROUND_DIRECTORY = "C:/Backgrounds";
//
//    public File getRandomFileFromDirectory(String directoryPath, int counter) {
//        File directory = new File(directoryPath);
//        File[] files = directory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".webp") || name.endsWith(".web"));
//
//        if (files == null || files.length == 0) {
//            return null;
//        }
//
//        return files[counter];
//    }
//
//    @PostMapping("/bulk-create")
//    public String bulkCreateBooks() throws IOException {
//
//        AtomicInteger counter = new AtomicInteger();
//
//        List<BookData> books = List.of(
//                new BookData("Moonlight Sorcery", "Epic Fantasy", "Elena Nightshade", "2021", "19", "200", "In a world where lunar magic shapes destinies, a young mage embarks on a perilous quest to uncover a secret that could alter the course of history. Mystical landscapes, magical creatures, and fierce battles await."),
//                new BookData("The Raven’s Secret", "Dark Mystery", "Samuel Drake", "2020", "15", "100", "A young ornithologist uncovers a society of intelligent ravens with the power to control fate. As dark secrets unfold, one mystery leads to another, blending intrigue with the supernatural."),
//                new BookData("Dragons of the Twilight Realm", "High Fantasy", "Adrian Blaze", "2022", "22", "27", "A dragon-wielding warrior is called to stop an ancient evil from resurrecting. In the twilight realm, dark forces prepare to unleash terror, and the hero must race against time."),
//                new BookData("Stormbreaker", "Post-Apocalyptic Thriller", "Jeffrey Black",  "2023", "25","23", "The world is in turmoil as violent storms ravage the land. A weather mage must unlock the power of the tempest to save humanity. Action-packed with supernatural abilities and explosive tension."),
//                new BookData("The Guardians of the Forest", "Mystical Fantasy", "Sophia Rivers", "2021", "18", "350", "A young girl with a gift to communicate with nature discovers the forest’s deepest secrets, where ravens guard ancient wisdom. In this enchanted world, magic is alive, and dangers lurk in every shadow."),
//                new BookData("Whispers in the Dark", "Gothic Fantasy", "M. Blackthorn", "2022", "20", "400", "A world consumed by dark magic and monstrous creatures, where the protagonist must uncover lost relics to challenge a powerful sorcerer. A chilling tale that lures you into its eerie embrace."),
//                new BookData("The Raven Chronicles", "Supernatural Thriller", "Catherine Moore", "2020", "17", "200", "When a detective stumbles upon a hidden world of ravens that possess unimaginable abilities, they must confront powerful forces that threaten not only the world of man but also that of myth."),
//                new BookData("Realm of Echoes", "Epic Fantasy", "Isla Dorne", "2022", "23", "117", "A hero's journey into the heart of a mystical land, filled with dragons, forgotten gods, and sinister prophecies. As ancient powers awaken, the protagonist is forced to rise against impossible odds."),
//                new BookData("Whispers of the Moonlit Forest", "Magical Realism", "Ariana Frost", "2021", "21", "154", "In a forgotten corner of the world, magical creatures live among the trees, but when a new threat emerges, a young girl must use her powers to communicate with them and protect their home."),
//                new BookData("The Sorcerer's Legacy", "Dark Fantasy", "Zara Night", "2022", "19", "646", "A young sorcerer discovers that their bloodline is entwined with a cursed legacy. A world of dark magic and forgotten gods beckons, where survival requires more than just power—it requires mastery of the unknown."),
//                new BookData("Enigma of the Shadows", "Supernatural Mystery", "Elijah Crowe", "2020", "17", "187", "A detective is pulled into a web of intrigue when an ancient secret society, hidden for centuries, is discovered. As dark forces rise, they must navigate a world where mystery and danger intertwine."),
//                new BookData("Journey Beyond the Stars", "Space Fantasy", "Marcus Grey", "2021", "20", "467", "In an age where starships journey across the galaxy, one hero must navigate cosmic storms, alien encounters, and ancient prophecies to uncover the secrets of a forgotten race."),
//                new BookData("The Chronicles of the Abyss", "Cosmic Horror", "Vincent Black", "2022", "18", "435", "The dark reaches of space hold unspeakable horrors. When a scientist discovers a portal to an unknown dimension, they awaken an ancient terror that threatens to consume the universe."),
//                new BookData("Shadows of the Past", "Gothic Thriller", "Nina Morrow", "2020", "19", "345", "A detective delves into the shadows of a dark, mysterious past, uncovering a secret that could bring an entire city to its knees."),
//                new BookData("The Midnight Gambit", "Thriller", "Lucinda Sterling", "2020", "17", "345", "A high-stakes poker game turns deadly when the stakes are more than just money—lives are on the line. As one player gets drawn into a dangerous game of manipulation and betrayal, they must navigate through a maze of lies and deception to uncover the real objective behind the game. With time running out and no one to trust, the only way out may be through the game itself. Will they survive, or will they become part of the deadly wager?"),
//                new BookData("Frostbite", "Sci-Fi Thriller", "Dylan Hurst", "2024", "23", "572", "In a dystopian future where Earth has entered a new ice age, survival depends on the ability to adapt. A group of scientists discovers a way to reverse the freezing process, but their discovery comes with a deadly price. As they race against time to save humanity, they must face the challenges of a frozen world and a growing conspiracy that threatens their every move. The cold is their greatest enemy, but betrayal may be even worse."),
//                new BookData("The Last Light", "Mystery", "Adelaide Storm", "2022", "18", "581", "When a renowned detective is found dead under mysterious circumstances, his protege takes on the case. As she dives deeper into his past, she uncovers connections to a series of unsolved crimes. Each clue brings her closer to a dangerous truth, but the closer she gets, the more powerful figures threaten her life. Will she unravel the mystery in time, or will she fall victim to the same shadow that claimed her mentor? A chilling tale of suspense and deception."),
//                new BookData("Tides of Destiny", "Historical Fantasy", "Eleanor Cole", "2023", "22", "317", "A sailor turned king embarks on a perilous journey to reclaim his stolen throne. In a world where the tides rule, his quest leads him through enchanted seas and forgotten lands, where ancient magic is the key to his success. But the deeper he dives into the mysteries of the past, the more he discovers that his enemies are not only human. This captivating historical fantasy explores love, betrayal, and the power of destiny."),
//                new BookData("Bound by Blood", "Horror", "Jonah Creed", "2022", "26", "456", "A small town is haunted by a series of gruesome murders that seem to follow a dark family lineage. As the investigation unfolds, the town's most prominent families are linked to the crimes, and the detectives find themselves caught in a deadly game of cat and mouse. With each clue that leads them closer to the truth, they must confront their darkest fears. Will they stop the killings before it's too late, or will they become the next victims of the blood-stained past?"),
//                new BookData("Shadows of the Past", "Gothic Thriller", "Nina Morrow", "2020", "19", "456", "A detective delves into the shadows of a dark, mysterious past, uncovering a secret that could bring an entire city to its knees. As he investigates, he stumbles upon a conspiracy that stretches far beyond the city's borders. As the body count rises, he realizes that the shadows hold far more than he expected, and the cost of uncovering the truth may be more than he's willing to pay. Will he survive the shadows, or will they consume him entirely?")
//        );
//
//
//
//        books.forEach(book -> {
//            File randomCover = getRandomFileFromDirectory(COVER_DIRECTORY, counter.get());
//            File randomBackground = getRandomFileFromDirectory(BACKGROUND_DIRECTORY, counter.get());
//
//            counter.getAndIncrement();
//
//            try {
//                bookService.createBook(book, randomBackground, randomCover);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
////
////        BulkRequest bulkRequest = BulkRequest.of(b -> b
////                .index("book_records")
////                .operations(books.stream()
////                        .map(book -> BulkOperation.of(op -> op
////                                .index(idx -> idx.document(book))))
////                        .collect(Collectors.toList()))
////        );
////
////        BulkResponse response = elasticsearchClient.bulk(bulkRequest);
////        return response.errors() ? "Bulk index failed!" : "Books indexed successfully!";
//        return "Good job!";
//    }
//
//}
//
