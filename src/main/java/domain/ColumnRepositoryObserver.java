package domain;

public interface ColumnRepositoryObserver {
    void onColumnChanged(Column changedColumn);
}