package data;

// Any object that wants to be notified when a journal's data has changed
// will implement this interface. 
public interface JournalDataChangeListener {
    public void dataChanged();
}
