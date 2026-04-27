package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ColumnRepositoryTest {

    private ColumnRepository repository;

    @BeforeEach
    public void setup() {
        repository = new ColumnRepository();
    }

    @Test
    public void testAddAndRemoveObserver() {
        ColumnRepositoryObserver observer = mock(ColumnRepositoryObserver.class);
        repository.addObserver(observer);
        repository.removeObserver(observer);
        repository.createNewColumn(); // Observer should not be notified
        verify(observer, never()).onColumnChanged(any());
    }

    @Test
    public void testRenameColumnAndNotify() {
        repository.createNewColumn();
        Column col = repository.getColumns().get(0);

        ColumnRepositoryObserver observer = mock(ColumnRepositoryObserver.class);
        repository.addObserver(observer);

        repository.rename(col, "RenamedColumn");

        assertEquals("RenamedColumn", col.getName());
        verify(observer).onColumnChanged(col);
    }

    @Test
    public void testRemoveColumnAndNotify() {
        repository.createNewColumn();
        Column col = repository.getColumns().get(0);

        ColumnRepositoryObserver observer = mock(ColumnRepositoryObserver.class);
        repository.addObserver(observer);

        repository.remove(col);

        assertFalse(repository.getColumns().contains(col));
        verify(observer).onColumnChanged(col);
    }

    @Test
    public void testGetType() {
        repository.createNewColumn();
        Column col = repository.getColumns().get(0);
        col.setType(ColumnType.INTEGER);

        assertEquals(ColumnType.INTEGER, repository.getType(0));
    }

    @Test
    public void testAllowsBlanks() {
        repository.createNewColumn();
        Column col = repository.getColumns().get(0);
        col.setBlanksAllowed(false);

        assertFalse(repository.allowsBlanks(0));
    }

    @Test
    public void testSetTypeUpdatesColumnAndNotifiesObserver() {
        Column col = repository.createNewColumn();
        ColumnRepositoryObserver observer = mock(ColumnRepositoryObserver.class);
        repository.addObserver(observer);

        repository.setType(col, ColumnType.INTEGER);

        assertEquals(ColumnType.INTEGER, col.getType());
        verify(observer).onColumnChanged(col);
    }

    @Test
    public void testAddObserver_Null_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> repository.addObserver(null));
    }

    @Test
    public void testRemoveObserver_Null_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> repository.removeObserver(null));
    }

    @Test
    public void testRename_NullColumn_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> repository.rename(null, "SomeName"));
    }

    @Test
    public void testRename_NullName_ThrowsException() {
        Column col = repository.createNewColumn();
        assertThrows(IllegalArgumentException.class, () -> repository.rename(col, null));
    }

    @Test
    public void testRemove_Null_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> repository.remove(null));
    }

    @Test
    public void testCreateMultipleColumnsGeneratesUniqueNames() {
        Column col1 = repository.createNewColumn();
        Column col2 = repository.createNewColumn();
        Column col3 = repository.createNewColumn();

        assertNotEquals(col1.getName(), col2.getName());
        assertNotEquals(col2.getName(), col3.getName());
        assertNotEquals(col1.getName(), col3.getName());
    }

}
