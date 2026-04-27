package ui;

import domain.Table;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import domain.ColumnRepository;

class SubwindowFactoryTest {

    private SubwindowFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SubwindowFactory();
    }

    @Test
    void createNewTableSubwindow_ReturnsTableSubwindowWithCorrectPositionAndSize() {
        TableSubwindow subwindow = factory.createNewTableSubwindow();

        assertNotNull(subwindow);
        assertEquals(50, subwindow.getX());
        assertEquals(50, subwindow.getY());
        assertEquals(300, subwindow.width);
        assertEquals(200, subwindow.height);
        assertEquals("Tables", subwindow.title);
    }

    @Test
    void createNewTableDesignSubwindow_ReturnsDesignSubwindowWithCorrectTitleAndPosition() {
        Table table = mock(Table.class);
        ColumnRepository columnRepository = mock(ColumnRepository.class);

        when(table.getName()).thenReturn("MockTable");
        when(table.getColumnRepository()).thenReturn(columnRepository);

        TableDesignSubwindow subwindow = factory.createNewTableDesignSubwindow(table);

        assertNotNull(subwindow);
        assertEquals(50, subwindow.getX());
        assertEquals(50, subwindow.getY());
        assertEquals(300, subwindow.width);
        assertEquals(200, subwindow.height);
        assertEquals("Table Design - MockTable", subwindow.title);
    }

    @Test
    void createNewTableRowsSubwindow_ReturnsRowsSubwindowWithCorrectTitleAndPosition() {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("MockTable");

        TableRowsSubwindow subwindow = factory.createNewTableRowsSubwindow(table);

        assertNotNull(subwindow);
        assertEquals(50, subwindow.getX());
        assertEquals(50, subwindow.getY());
        assertEquals(300, subwindow.width);
        assertEquals(200, subwindow.height);
        assertEquals("Table Rows - MockTable", subwindow.title);
    }

    @Test
    void createdCount_IncrementsCorrectlyAcrossMultipleCreations() {
        Table table = mock(Table.class);
        ColumnRepository columnRepo = mock(ColumnRepository.class);
        when(table.getName()).thenReturn("Test");
        when(table.getColumnRepository()).thenReturn(columnRepo);

        Subwindow sub1 = factory.createNewTableSubwindow(); // offset = 0
        Subwindow sub2 = factory.createNewTableDesignSubwindow(table); // offset = 20
        Subwindow sub3 = factory.createNewTableRowsSubwindow(table); // offset = 40

        assertEquals(50, sub1.getX());
        assertEquals(70, sub2.getX());
        assertEquals(90, sub3.getX());
    }

}
