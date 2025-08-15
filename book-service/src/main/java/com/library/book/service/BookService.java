package com.library.book.service;

import com.library.book.dto.BookDto;
import com.library.book.entity.Book;
import com.library.book.repository.BookRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    public Book createBook(BookDto bookDto) {
        if (bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            throw new RuntimeException("Book with ISBN " + bookDto.getIsbn() + " already exists!");
        }
        
        Book book = convertDtoToEntity(bookDto);
        Book savedBook = bookRepository.save(book);
        
        return savedBook;
    }
    
    public List<Book> createBooksFromCsv(MultipartFile file) throws IOException, CsvException {
        List<Book> books = new ArrayList<>();
        
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = csvReader.readAll();
            
            // Skip header row if present
            boolean skipHeader = true;
            
            for (String[] record : records) {
                if (skipHeader) {
                    skipHeader = false;
                    // Check if first row is header by looking for non-numeric values in expected numeric fields
                    try {
                        Integer.parseInt(record[5]); // Try to parse totalCopies
                        // If successful, this is data row, process it
                        skipHeader = false;
                        Book book = parseCsvRecord(record);
                        if (book != null) {
                            books.add(book);
                        }
                    } catch (NumberFormatException e) {
                        // This is likely a header row, continue to next
                        continue;
                    }
                } else {
                    Book book = parseCsvRecord(record);
                    if (book != null) {
                        books.add(book);
                    }
                }
            }
        }
        
        List<Book> savedBooks = bookRepository.saveAll(books);
        
        return savedBooks;
    }
    
    private Book parseCsvRecord(String[] record) {
        try {
            // Expected CSV format: ISBN, Title, Author, Category, Publisher, TotalCopies, Price, PublicationYear, Pages, Language, Description
            if (record.length < 6) {
                return null; // Skip incomplete records
            }
            
            Book book = new Book();
            book.setIsbn(record[0].trim());
            book.setTitle(record[1].trim());
            book.setAuthor(record[2].trim());
            book.setCategory(record[3].trim());
            
            if (record.length > 4 && !record[4].trim().isEmpty()) {
                book.setPublisher(record[4].trim());
            }
            
            if (record.length > 5 && !record[5].trim().isEmpty()) {
                book.setTotalCopies(Integer.parseInt(record[5].trim()));
                book.setAvailableCopies(book.getTotalCopies());
            }
            
            if (record.length > 6 && !record[6].trim().isEmpty()) {
                book.setPrice(new BigDecimal(record[6].trim()));
            }
            
            if (record.length > 7 && !record[7].trim().isEmpty()) {
                book.setPublicationYear(Integer.parseInt(record[7].trim()));
            }
            
            if (record.length > 8 && !record[8].trim().isEmpty()) {
                book.setPages(Integer.parseInt(record[8].trim()));
            }
            
            if (record.length > 9 && !record[9].trim().isEmpty()) {
                book.setLanguage(record[9].trim());
            }
            
            if (record.length > 10 && !record[10].trim().isEmpty()) {
                book.setDescription(record[10].trim());
            }
            
            book.setStatus(Book.BookStatus.AVAILABLE);
            
            // Check if book with same ISBN already exists
            if (bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
                return null; // Skip duplicate ISBN
            }
            
            return book;
            
        } catch (NumberFormatException e) {
            // Skip malformed records
            return null;
        }
    }
    
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
    
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
    
    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }
    
    public List<Book> findAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
    
    public List<Book> searchBooks(String searchTerm) {
        return bookRepository.searchBooks(searchTerm);
    }
    
    public List<Book> findBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }
    
    public List<Book> findBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }
    
    public List<String> getAllCategories() {
        return bookRepository.findAllCategories();
    }
    
    public List<String> getAllAuthors() {
        return bookRepository.findAllAuthors();
    }
    
    public List<String> getAllPublishers() {
        return bookRepository.findAllPublishers();
    }
    
    public Book updateBook(Long id, BookDto bookDto) {
        return bookRepository.findById(id)
                .map(book -> {
                    updateBookFromDto(book, bookDto);
                    return bookRepository.save(book);
                })
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }
    
    public Book updateInventory(Long id, Integer totalCopies, Integer availableCopies) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setTotalCopies(totalCopies);
                    book.setAvailableCopies(availableCopies);
                    return bookRepository.save(book);
                })
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }
    
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }
    
    public List<Book> getLowStockBooks(Integer threshold) {
        return bookRepository.findLowStockBooks(threshold);
    }
    
    public List<Book> getOutOfStockBooks() {
        return bookRepository.findOutOfStockBooks();
    }
    
    // Analytics support methods
    public long getTotalBooksCount() {
        return bookRepository.count();
    }
    
    public long getAvailableBooksCount() {
        return bookRepository.countByAvailableCopiesGreaterThan(0);
    }
    
    public long getTotalCopies() {
        return bookRepository.getTotalCopies();
    }
    
    public long getTotalAvailableCopies() {
        return bookRepository.getTotalAvailableCopies();
    }
    
    public List<java.util.Map<String, Object>> getBookCountByCategory() {
        return bookRepository.getBookCountByCategory();
    }
    
    public List<Object[]> getPopularBooks() {
        // Return most available books as popular (placeholder)
        return bookRepository.getPopularBooks();
    }
    
    public List<Object[]> getRecentlyAddedBooks() {
        return bookRepository.getRecentlyAddedBooks();
    }
    
    // Book inventory update methods for Transaction Service
    public void borrowBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for borrowing");
        }
        
        book.borrowCopy();
        bookRepository.save(book);
    }
    
    public void returnBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        book.returnCopy();
        bookRepository.save(book);
    }
    
    private Book convertDtoToEntity(BookDto dto) {
        Book book = new Book();
        updateBookFromDto(book, dto);
        return book;
    }
    
    private void updateBookFromDto(Book book, BookDto dto) {
        book.setIsbn(dto.getIsbn());
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setCategory(dto.getCategory());
        book.setPublisher(dto.getPublisher());
        book.setPublicationYear(dto.getPublicationYear());
        book.setDescription(dto.getDescription());
        book.setTotalCopies(dto.getTotalCopies());
        if (dto.getAvailableCopies() != null) {
            book.setAvailableCopies(dto.getAvailableCopies());
        } else {
            book.setAvailableCopies(dto.getTotalCopies());
        }
        book.setPrice(dto.getPrice());
        book.setLanguage(dto.getLanguage());
        book.setPages(dto.getPages());
        if (dto.getStatus() != null) {
            book.setStatus(Book.BookStatus.valueOf(dto.getStatus().toUpperCase()));
        }
    }
}
