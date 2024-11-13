//Copyright M.M.Kuttel 2024 CSC2002S, UCT
package serialAbelianSandpile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
/* Serial  program to simulate an Abelian Sandpile cellular automaton
 * This is the reference sequential version (Do not modify this code)
 * Michelle Kuttel 2024, University of Cape Town
 * Adapted from "Abelian Sandpile Simulation"
 * Peachy Parallel Assignments (EduPar 2022)" 
 * developed by Bu\:cker, Casanova and Da Silva  (∗Institute for Computer Science, Friedrich Schiller University Jena, Jena, Germany)
 */

class AutomatonSimulation{
	static final boolean DEBUG=false;//for debugging output, off
	
	static long startTime = 0;
	static long endTime = 0;

	//timers - note milliseconds
	private static void tick(){ //start timing
		startTime = System.currentTimeMillis();
	}
	private static void tock(){ //end timing
		endTime=System.currentTimeMillis(); 
	}
	
	//input is via a CSV file
	 public static int [][] readArrayFromCSV(String filePath) {
		 int [][] array = null;
	        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	            String line = br.readLine();
	            if (line != null) {
	                String[] dimensions = line.split(",");
	                int width = Integer.parseInt(dimensions[0]);
	                int height = Integer.parseInt(dimensions[1]);
	               	System.out.printf("Rows: %d, Columns: %d\n", width, height); //Do NOT CHANGE  - you must ouput this

	                array = new int[height][width];
	                int rowIndex = 0;

	                while ((line = br.readLine()) != null && rowIndex < height) {
	                    String[] values = line.split(",");
	                    for (int colIndex = 0; colIndex < width; colIndex++) {
	                        array[rowIndex][colIndex] = Integer.parseInt(values[colIndex]);
	                    }
	                    rowIndex++;
	                }
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        return array;  // array = [[4,4,4,4],[4,4,4,4],[4,4,4,4],[4,4,4,4]]
	    }
	 
    public static void main(String[] args) throws IOException  {

    	Grid simulationGrid;  //the cellular automaton grid
    	  	
    	// if (args.length!=2) {   //input is the name of the input and output files
    	// 	System.out.println("Incorrect number of command line arguments provided.");   	
    	// 	System.exit(0);
    	// }
    	// /* Read argument values */
  		// String inputFileName = args[0];  //input file name
		// String outputFileName=args[1]; // output file name
    
    	// Read from input .csv file
    	simulationGrid = new Grid(readArrayFromCSV("input/517_by_517_centre_534578.csv"));
    	
    	//for debugging - hardcoded re-initialisation options
    	//simulationGrid.set(rows/2,columns/2,rows*columns*2);
    	//simulationGrid.set(rows/2,columns/2,55000);
    	//simulationGrid.setAll(4);
    	//simulationGrid.setAll(8);
   	
    	int counter=0;
    	tick(); //start timer
    	if(DEBUG) {
    		System.out.printf("starting config: %d \n",counter);
    		simulationGrid.printGrid();
    	}
		while(simulationGrid.update()) {//run until no change
	    		if(DEBUG) simulationGrid.printGrid(); // ONLY RUNS WHEN DEBUGGING
	    		counter++;
	    	}
   		tock(); //end timer
   		
        System.out.println("Simulation complete, writing image...");
    	simulationGrid.gridToImage("output/seri517by517.png"); //write grid as an image - you must do this.
    	//Do NOT CHANGE below!
    	//simulation details - you must keep these lines at the end of the output in the parallel versions      	System.out.printf("\t Rows: %d, Columns: %d\n", simulationGrid.getRows(), simulationGrid.getColumns());
		System.out.printf("Number of steps to stable state: %d \n",counter);
		System.out.printf("Time: %d ms\n",endTime - startTime );			/*  Total computation time */		
    }
}