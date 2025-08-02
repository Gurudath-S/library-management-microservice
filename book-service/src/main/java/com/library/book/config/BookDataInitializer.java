package com.library.book.config;

import com.library.book.entity.Book;
import com.library.book.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class BookDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(BookDataInitializer.class);
    
    @Autowired
    private BookRepository bookRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Book Service data initialization...");
        initializeBooks();
        logDataSummary();
        logger.info("Book Service data initialization completed!");
    }
    
    private void initializeBooks() {
        if (bookRepository.count() > 0) {
            logger.info("Books already exist, skipping book initialization");
            return;
        }
        
        logger.info("Creating test books...");
        
        // Complete book data from monolith for comprehensive analytics
        Object[][] bookData = {
            // {ISBN, Title, Author, Category, Publisher, Year, Copies, Price, Pages}
            {"978-0134685991", "Effective Java", "Joshua Bloch", "Programming", "Addison-Wesley", 2017, 5, "45.99", 412},
            {"978-0321356680", "Effective C++", "Scott Meyers", "Programming", "Addison-Wesley", 2005, 3, "42.99", 320},
            {"978-0596009205", "Head First Design Patterns", "Eric Freeman", "Programming", "O'Reilly", 2004, 4, "39.99", 694},
            {"978-0132350884", "Clean Code", "Robert Martin", "Programming", "Prentice Hall", 2008, 6, "44.99", 464},
            {"978-0201633610", "Design Patterns", "Gang of Four", "Programming", "Addison-Wesley", 1994, 2, "54.99", 395},
            
            {"978-0061120084", "To Kill a Mockingbird", "Harper Lee", "Fiction", "Harper Perennial", 1960, 8, "14.99", 376},
            {"978-0451524935", "1984", "George Orwell", "Fiction", "Signet Classics", 1949, 7, "13.99", 328},
            {"978-0547928227", "The Hobbit", "J.R.R. Tolkien", "Fantasy", "Houghton Mifflin", 1937, 5, "16.99", 310},
            {"978-0439708180", "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy", "Scholastic", 1997, 10, "8.99", 309},
            {"978-0316769174", "The Catcher in the Rye", "J.D. Salinger", "Fiction", "Little Brown", 1951, 4, "15.99", 234},
            {"978-0486284736", "The Adventures of Sherlock Holmes", "Arthur Conan Doyle", "Mystery", "Dover Publications", 1892, 6, "12.99", 307},
            {"978-0345339706", "The Lion, the Witch and the Wardrobe", "C.S. Lewis", "Fantasy", "HarperCollins", 1950, 5, "8.99", 208},
            {"978-0743273565", "The Great Gatsby", "F. Scott Fitzgerald", "Fiction", "Scribner", 1925, 7, "15.99", 180},
            {"978-0525478812", "The Fault in Our Stars", "John Green", "Romance", "Dutton Books", 2012, 8, "12.99", 313},
            {"978-0060935467", "One Hundred Years of Solitude", "Gabriel García Márquez", "Fiction", "Harper Perennial", 1967, 3, "17.99", 417},
            
            {"978-0199536566", "Oxford History of the World", "J.M. Roberts", "History", "Oxford University Press", 2013, 2, "29.99", 984},
            {"978-0143036531", "The Immortal Life of Henrietta Lacks", "Rebecca Skloot", "Science", "Broadway Books", 2010, 4, "16.99", 381},
            {"978-0385537859", "Freakonomics", "Steven Levitt", "Economics", "William Morrow", 2005, 5, "16.99", 315},
            {"978-1400063515", "The Tipping Point", "Malcolm Gladwell", "Psychology", "Little Brown", 2000, 6, "17.99", 301},
            {"978-0062316097", "Sapiens", "Yuval Noah Harari", "History", "Harper", 2015, 9, "22.99", 443},
            
            {"978-0446310789", "To Kill a Mockingbird", "Harper Lee", "Fiction", "Grand Central", 1960, 3, "14.99", 281},
            {"978-0062073488", "Gone Girl", "Gillian Flynn", "Thriller", "Crown Publishers", 2012, 7, "15.99", 419},
            {"978-0385514231", "Water for Elephants", "Sara Gruen", "Fiction", "Algonquin Books", 2006, 4, "15.99", 331},
            {"978-0062315007", "The Alchemist", "Paulo Coelho", "Fiction", "HarperOne", 1988, 6, "14.99", 163},
            {"978-0553296983", "Dune", "Frank Herbert", "Science Fiction", "Ace", 1965, 5, "16.99", 688},
            
            {"978-0553213119", "Jurassic Park", "Michael Crichton", "Science Fiction", "Ballantine Books", 1990, 4, "15.99", 399},
            {"978-0307277671", "The Da Vinci Code", "Dan Brown", "Thriller", "Doubleday", 2003, 8, "15.99", 454},
            {"978-0307269751", "The Girl with the Dragon Tattoo", "Stieg Larsson", "Thriller", "Knopf", 2005, 6, "14.99", 644},
            {"978-0439655484", "The Hunger Games", "Suzanne Collins", "Young Adult", "Scholastic", 2008, 9, "13.99", 374},
            {"978-0439139595", "Harry Potter and the Goblet of Fire", "J.K. Rowling", "Fantasy", "Scholastic", 2000, 8, "10.99", 734},
            {"978-0060850524", "Brave New World", "Aldous Huxley", "Science Fiction", "Harper Perennial", 1932, 5, "15.99", 311},
            {"978-0140449136", "The Count of Monte Cristo", "Alexandre Dumas", "Adventure", "Penguin Classics", 1844, 2, "18.99", 1276},
            {"978-0486411095", "Frankenstein", "Mary Shelley", "Horror", "Dover Publications", 1818, 4, "12.99", 166},
            {"978-0486406510", "Dracula", "Bram Stoker", "Horror", "Dover Publications", 1897, 3, "13.99", 418},
            {"978-0140283334", "Les Misérables", "Victor Hugo", "Fiction", "Penguin Classics", 1862, 2, "22.99", 1463},
            
            {"978-0553382563", "A Game of Thrones", "George R.R. Martin", "Fantasy", "Bantam", 1996, 7, "16.99", 694},
            {"978-0345391803", "The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Science Fiction", "Del Rey", 1979, 6, "14.99", 224},
            {"978-0441013593", "Neuromancer", "William Gibson", "Cyberpunk", "Ace", 1984, 3, "15.99", 271},
            {"978-0553573404", "A Clash of Kings", "George R.R. Martin", "Fantasy", "Bantam", 1999, 5, "16.99", 761},
            {"978-0765326355", "The Way of Kings", "Brandon Sanderson", "Fantasy", "Tor Books", 2010, 4, "28.99", 1007},
            
            {"978-0316015844", "Twilight", "Stephenie Meyer", "Romance", "Little Brown", 2005, 6, "12.99", 498},
            {"978-0439023481", "The Giver", "Lois Lowry", "Young Adult", "Houghton Mifflin", 1993, 5, "8.99", 180},
            {"978-0062024039", "Divergent", "Veronica Roth", "Young Adult", "Katherine Tegen", 2011, 7, "17.99", 487},
            {"978-0385737951", "The Maze Runner", "James Dashner", "Young Adult", "Delacorte Press", 2009, 6, "9.99", 375},
            {"978-0545010221", "The Lightning Thief", "Rick Riordan", "Young Adult", "Disney-Hyperion", 2005, 8, "7.99", 377},
            
            {"978-0590353427", "Hatchet", "Gary Paulsen", "Adventure", "Scholastic", 1987, 4, "8.99", 195},
            {"978-0064401944", "Where the Red Fern Grows", "Wilson Rawls", "Adventure", "Yearling", 1961, 3, "8.99", 245},
            {"978-0394800011", "The Cat in the Hat", "Dr. Seuss", "Children", "Random House", 1957, 5, "8.99", 61},
            {"978-0064430937", "Charlotte's Web", "E.B. White", "Children", "HarperCollins", 1952, 6, "7.99", 184},
            {"978-0439064873", "Captain Underpants", "Dav Pilkey", "Children", "Blue Sky Press", 1997, 4, "5.99", 125},
            {"978-0061353246", "Coraline", "Neil Gaiman", "Fantasy", "HarperCollins", 2002, 4, "8.99", 162},
            {"978-0142407332", "The Kite Runner", "Khaled Hosseini", "Fiction", "Riverhead Books", 2003, 5, "15.99", 371}
        };
        
        List<Book> books = new ArrayList<>();
        
        for (Object[] data : bookData) {
            Book book = new Book();
            book.setIsbn((String) data[0]);
            book.setTitle((String) data[1]);
            book.setAuthor((String) data[2]);
            book.setCategory((String) data[3]);
            book.setPublisher((String) data[4]);
            book.setPublicationYear((Integer) data[5]);
            book.setTotalCopies((Integer) data[6]);
            book.setAvailableCopies((Integer) data[6]); // Start with all copies available
            book.setPrice(new BigDecimal((String) data[7]));
            book.setPages((Integer) data[8]);
            book.setLanguage("English");
            book.setStatus(Book.BookStatus.AVAILABLE);
            book.setDescription("A great book in the " + data[3] + " category by " + data[2]);
            book.setCreatedAt(LocalDateTime.now());
            book.setUpdatedAt(LocalDateTime.now());
            
            books.add(book);
        }
        
        bookRepository.saveAll(books);
        logger.info("Created {} books successfully", books.size());
    }
    
    private void logDataSummary() {
        long bookCount = bookRepository.count();
        long totalCopies = bookRepository.getTotalCopies();
        long availableCopies = bookRepository.getTotalAvailableCopies();
        List<java.util.Map<String, Object>> categoryData = bookRepository.getBookCountByCategory();
        
        logger.info("=== BOOK SERVICE DATA SUMMARY ===");
        logger.info("Total books created: {}", bookCount);
        logger.info("Total copies: {}", totalCopies);
        logger.info("Available copies: {}", availableCopies);
        logger.info("Categories: {}", categoryData.size());
        logger.info("=================================");
    }
}
