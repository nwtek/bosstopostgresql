import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelFileReader {

	public static void main(String[] args) throws IOException {
			try {
			    //POIFSFileSystem fs = new POIFSFileSystem();
			    HSSFWorkbook wb = new HSSFWorkbook();
			    HSSFSheet sheet = wb.getSheetAt(0);
			    HSSFRow row;
			    HSSFCell cell;

			    int rows; // No of rows
			    rows = sheet.getPhysicalNumberOfRows();

			    int cols = 0; // No of columns
			    int tmp = 0;

			    // This trick ensures that we get the data properly even if it doesn't start from first few rows
			    for(int i = 0; i < 10 || i < rows; i++) {
			        row = sheet.getRow(i);
			        if(row != null) {
			            tmp = sheet.getRow(i).getPhysicalNumberOfCells();
			            if(tmp > cols) cols = tmp;
			        }
			    }

			    for(int r = 0; r < rows; r++) {
			        row = sheet.getRow(r);
			        if(row != null) {
			            for(int c = 0; c < cols; c++) {
			                cell = row.getCell((short)c);
			                if(cell != null) {
			                    // Your code here
			                }
			            }
			        }
			    }
			} catch(Exception ioe) {
			    ioe.printStackTrace();
			}

	}
	
	public static void createFile(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Workbook wb = new HSSFWorkbook();
	    //Workbook wb = new XSSFWorkbook();
	    CreationHelper createHelper = wb.getCreationHelper();
	    Sheet sheet = wb.createSheet("new sheet");

	    // Create a row and put some cells in it. Rows are 0 based.
	    Row row = sheet.createRow((short)0);
	    // Create a cell and put a value in it.
	    Cell cell = row.createCell(0);
	    cell.setCellValue(1);

	    // Or do it on one line.
	    row.createCell(1).setCellValue(1.2);
	    row.createCell(2).setCellValue(
	         createHelper.createRichTextString("This is a string"));
	    row.createCell(3).setCellValue(true);

	    // Write the output to a file
	    FileOutputStream fileOut = new FileOutputStream("workbook.xls");
	    wb.write(fileOut);
	    fileOut.close();
	}

}
