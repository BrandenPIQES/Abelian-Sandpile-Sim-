// Author: Phathutshedzo Netshitangani,NTSPHA021
// A parallel solution that draws albelian sandpiles 
// from different grid sizes
package parallelAbelianSandpile; 

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelAutomaton {
    static final boolean DEBUG = false; // for debugging output, off

    static long startTime = 0;
    static long endTime = 0;

    // timers - note milliseconds
    private static void tick() { // start timing
        startTime = System.currentTimeMillis();
    }

    private static void tock() { // end timing
        endTime = System.currentTimeMillis();
    }

    // input is via a CSV file
    public static int[][] readArrayFromCSV(String filePath) {
        int[][] array = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line != null) {
                String[] dimensions = line.split(",");
                int width = Integer.parseInt(dimensions[0]);
                int height = Integer.parseInt(dimensions[1]);
                System.out.printf("Rows: %d, Columns: %d\n", width, height); // Do NOT CHANGE - you must output this

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

        return array; 
    }

    public static void main(String[] args) throws IOException {

        Grid simulationGrid; // the cellular automaton grid

        if (args.length!=2) { //input is the name of the input and output files
        System.out.println("Incorrect number of command line arguments provided.");
        System.exit(0);
        }
        // /* Read argument values */
        String inputFileName = args[0]; //input file name
        String outputFileName=args[1]; // output file name

        // Read from input .csv file
        simulationGrid = new Grid(readArrayFromCSV(inputFileName));

        int counter = 0;
        tick(); // start timer
        
        ForkJoinPool pool = new ForkJoinPool();
        while (simulationGrid.update(pool)) { // run until no change
            if (DEBUG)
                simulationGrid.printGrid(); // ONLY RUNS WHEN DEBUGGING
            counter++;
        }
        tock(); // end timer

        System.out.println("Simulation complete, writing image...");
        simulationGrid.gridToImage(outputFileName); 
        System.out.printf("Number of steps to stable state: %d \n", counter);
        System.out.printf("Time: %d ms\n", endTime - startTime); /* Total computation time */
    }

    // Fork/Join task to update the grid in parallel
    static class UpdateAction extends RecursiveAction {
        private static final int THRESHOLD = 10000; // threshold for splitting tasks
        private final int[][] grid;
        private final int[][] updateGrid;
        private final int startRow, endRow;
        private final int startCol, endCol;
        private boolean change = false;

        public UpdateAction(int[][] grid, int[][] updateGrid, int startRow, int endRow, int startCol, int endCol) {
            this.grid = grid;
            this.updateGrid = updateGrid;
            this.startRow = startRow;
            this.endRow = endRow;
            this.startCol = startCol;
            this.endCol = endCol;
        }

        public boolean hasChanged() {
            return change;
        }

        @Override
        protected void compute() {
            if ((endRow - startRow) * (endCol - startCol) <= THRESHOLD) {
                // If a grid is small enough we just compute it
                // do not update border
                for (int i = startRow; i < endRow; i++) {
                    for (int j = startCol; j < endCol; j++) {
                        updateGrid[i][j] = (grid[i][j] % 4) +
                                (grid[i - 1][j] / 4) +
                                (grid[i + 1][j] / 4) +
                                (grid[i][j - 1] / 4) +
                                (grid[i][j + 1] / 4);
                        if (grid[i][j] != updateGrid[i][j]) {
                            change = true;
                        }
                    }
                }
            } else {
                //divide the grid into 4 approx equal parts
                int midRow = (startRow + endRow) / 2;
                int midCol = (startCol + endCol) / 2;
                UpdateAction topLeft = new UpdateAction(grid, updateGrid, startRow, midRow, startCol, midCol);
                UpdateAction topRight = new UpdateAction(grid, updateGrid, startRow, midRow, midCol, endCol);
                UpdateAction bottomLeft = new UpdateAction(grid, updateGrid, midRow, endRow, startCol, midCol);
                UpdateAction bottomRight = new UpdateAction(grid, updateGrid, midRow, endRow, midCol, endCol);

                // computes the four sub-grids, at once
                topLeft.fork();
                topRight.fork();
                bottomLeft.fork();
                bottomRight.fork();

                // We wait for each one to finish
                topLeft.join();
                topRight.join();
                bottomLeft.join();
                bottomRight.join();

                change = topLeft.hasChanged() || topRight.hasChanged() || bottomLeft.hasChanged() || bottomRight.hasChanged();
                //if any of the sub-cells has changed, the simulation goes on.
                // in the main this will be called again
            }
        }
    }
}
