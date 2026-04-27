package ui.editors;

import java.util.List;

public interface NameAccess<T> {
    String getName(T item);
    void setName(T item, String name);
    List<T> getAll();
}