package mate.academy.dao;

import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.Book;
import mate.academy.util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Dao
public class BookDaoImpl implements BookDao {
    @Override
    public Book create(Book book) {
        String query = "INSERT INTO books (title, price) VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, book.getTitle());
            statement.setBigDecimal(2, book.getPrice());

            int affectedRows = statement.executeUpdate();
            if (affectedRows < 1) {
                throw new RuntimeException("Expected to insert at least 1 row, but inserted 0 rows.");
            }

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Long id = generatedKeys.getObject(1, Long.class);
                book.setId(id);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can`t get a new Book: " + book, e);
        }
        return book;
    }

    @Override
    public Optional<Book> findById(Long id) {
        String query = "SELECT * FROM books WHERE id = ?";
        Book bookFromDb = null;
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, 3L);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                bookFromDb = parseResultSetInBook(resultSet);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can not find Book with id: " + id, e);
        }
        return Optional.ofNullable(bookFromDb);
    }

    @Override
    public List<Book> findAll() {
        String query = "SELECT * FROM books";
        List<Book> allBooksFromDb = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                allBooksFromDb.add(parseResultSetInBook(resultSet));
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can`t get all elements from DB.", e);
        }
        return allBooksFromDb;
    }

    @Override
    public Book update(Book book) {
        String query = "UPDATE books SET title = ?, price = ? WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, book.getTitle());
            statement.setBigDecimal(2, book.getPrice());
            statement.setLong(3, book.getId());

            int affectedRow = statement.executeUpdate();
            if (affectedRow < 1) {
                throw new RuntimeException("Expected to update at least 1 row but was 0.");
            }

        } catch (SQLException e) {
            throw new DataProcessingException("Can not update row by the book with id: " + book.getId(), e);
        }
        return book;
    }

    @Override
    public boolean deleteById(Long id) {
        String query = "DELETE FROM books WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, 6L);

            int affectedRow = statement.executeUpdate();
            if (affectedRow < 1) {
                throw new RuntimeException("Expected to delete at least 1 row but was 0.");
            }
            return affectedRow > 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Can not delete row by the id: " + id, e);
        }
    }


    private Book parseResultSetInBook(ResultSet resultSet) throws SQLException {
        Book dbBook = new Book();
        dbBook.setId(resultSet.getLong("id"));
        dbBook.setTitle(resultSet.getString("title"));
        dbBook.setPrice(resultSet.getBigDecimal("price"));
        return dbBook;
    }
}
