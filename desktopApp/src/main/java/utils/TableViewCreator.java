package utils;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import desktopApp.Registry;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TableViewCreator {
	/**
		 * 
		 * @param <C> the class for which the table is made
		 * @param table
		 * @param names
		 * @param propertyNames
		 * @throws IllegalArgumentException
		 */
		public static <C> void initializeTable(TableView<C> table,
				String[] names, String[] propertyNames, Class<C> clas) {
			if(names.length != propertyNames.length)
				throw new IllegalArgumentException();
			
			ObservableList<TableColumn<C, ?>> cols = table.getColumns();
			for(int i = 0; i< names.length; i++) {
				try {
					cols.add(createColumn(names[i], propertyNames[i], clas));
				} catch (NoSuchFieldException | SecurityException e) {
					log.error("Error creating a column", e);
				}
			}
		}
		
		
		/**
		 * 
		 * @param <C> the class for which the table is made
		 * @param <T> the type to be shown
		 * @param name
		 * @param propertyName
		 * @return
		 * @throws SecurityException 
		 * @throws NoSuchFieldException 
		 */
		private static <C, T> TableColumn<C, T> createColumn(String name, String propertyName,
				Class<C> clas) throws NoSuchFieldException, SecurityException {
			TableColumn<C, T> col = new TableColumn<>(name);
			
			//the value for each item will be retrieved from the given property
			//of object of type C
			col.setCellValueFactory(new PropertyValueFactory<>(propertyName));
			
			
			//use reflection to check if the data type is Instant to use a formatter
			//for its "rendering"
			Type dataType = clas.getDeclaredField(propertyName).getType();
			if(dataType.equals(Instant.class)) {
				col.setCellFactory(column -> {
				    TableCell<C, Instant> cell = new TableCell<C, Instant>() {

				    	DateTimeFormatter dateTimeFormatter = Registry.getInstance()
				    			.getDateTimeFormatter();
				    	
				        @Override
				        protected void updateItem(Instant item, boolean empty) {
				            super.updateItem(item, empty);
				            
				            if(empty || item == null) {
				                setText(null);
				            }
				            else {
				            	log.info(item);
				                setText(dateTimeFormatter.format(item));
				            }
				        }
				    };

				    return (TableCell<C, T>) cell;
				});
			}
			
			return col;
		}
}

