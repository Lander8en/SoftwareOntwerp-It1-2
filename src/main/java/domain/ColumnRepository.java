package domain;

import java.util.ArrayList;
import java.util.List;

import domain.naming.NameGenerator;
import domain.naming.SequentialNamingStrategy;

/**
 * Manages the collection of columns in a table and notifies observers about changes.
 */
public class ColumnRepository {

    private final List<Column> columns = new ArrayList<>();
    private final List<ColumnRepositoryObserver> observers = new ArrayList<>();
    private final NameGenerator nameGenerator = new NameGenerator(new SequentialNamingStrategy());

    /**
     * Registers an observer to be notified when a column changes.
     *
     * @param observer the observer to add, must not be null
     * @throws IllegalArgumentException if observer is null
     */
    public void addObserver(ColumnRepositoryObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer must not be null");
        }
        observers.add(observer);
    }

    /**
     * Unregisters a previously registered observer.
     *
     * @param observer the observer to remove, must not be null
     * @throws IllegalArgumentException if observer is null
     */
    public void removeObserver(ColumnRepositoryObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer must not be null");
        }
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers that a column has changed.
     *
     * @param changedColumn the column that changed
     */
    private void notifyObservers(Column changedColumn) {
        for (ColumnRepositoryObserver observer : observers) {
            observer.onColumnChanged(changedColumn);
        }
    }

    /**
     * Creates a new column with a unique name and adds it to the list.
     *
     * @return the newly created column
     */
    public Column createNewColumn() {
        String name = nameGenerator.generateUniqueName("Column", columns);
        Column newCol = new Column(name);
        columns.add(newCol);
        //notifyObservers(newCol);
        return newCol;
    }

    /**
     * Returns the list of columns in this repository.
     *
     * @return the list of columns
     */
    public List<Column> getColumns() {
        return this.columns;
    }

    /**
     * Renames the given column and notifies observers.
     *
     * @param column the column to rename, must not be null
     * @param name   the new name to set, must not be null
     * @throws IllegalArgumentException if column or name is null
     */
    public void rename(Column column, String name) {
        if (column == null || name == null) {
            throw new IllegalArgumentException("Column and name must not be null");
        }
        column.setName(name);
        notifyObservers(column);
    }

    /**
     * Removes the given column from the repository and notifies observers.
     *
     * @param column the column to remove, must not be null
     * @throws IllegalArgumentException if column is null
     */
    public void remove(Column column) {
        if (column == null) {
            throw new IllegalArgumentException("Column must not be null");
        }
        columns.remove(column);
        //notifyObservers(column);
    }

    /**
     * Returns the type of the column at the specified index.
     *
     * @param colIndex the column index
     * @return the column type
     */
    public ColumnType getType(int colIndex) {
        return columns.get(colIndex).getType();
    }

    /**
     * Returns whether blanks are allowed in the column at the specified index.
     *
     * @param colIndex the column index
     * @return true if blanks are allowed, false otherwise
     */
    public boolean allowsBlanks(int colIndex) {
        return columns.get(colIndex).isBlanksAllowed();
    }

    /**
     * Updates the type of a column and notifies all observers.
     *
     * @param column the column to update
     * @param newType the new type to assign
     */
    public void setType(Column column, ColumnType newType) {
        column.setType(newType);
        notifyObservers(column);
    }
}