import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class sortingExperiment {
    Random stdRandom = new Random();
    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE = 2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 100;
    static int MAXINPUTSIZE  = (int) Math.pow(2,14);
    static int MININPUTSIZE  =  1;
    private static long[] aux;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/karson/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {


        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        runFullExperiment("quickSort-Exp1-ThrowAway.txt");
        runFullExperiment("quickSort-Exp2.txt");
        runFullExperiment("quickSort-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName){
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");
            long[] testList = createSortedntegerList(inputSize);
            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random key to search in the range of a the min/max numbers in the list
                long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */


                //The function calls for different sorts, remove "//" from function call to use it..
                //bubbleSort(testList);
                //insertionSort(testList);
                //mergeSort(testList);
                quickSort(testList);
                //naiveQuickSort(testList);

                //long foundIndex = ThreeSum(testList);




                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    /* return index of the searched number if found, or -1 if not found */

    public static long[] createSortedntegerList(int size) {
        long[] newList = new long[size];
        newList[0] = (long) (10 * Math.random());
        for (int j = 1; j < size; j++) {
            newList[j] = newList[j - 1] + (long) (10 * Math.random());
            //if(j%100000 == 0) {resultsWriter.printf("%d  %d <<<\n",j,newList[j]);  resultsWriter.flush();}
        }
        return newList;
    }

    public static void bubbleSort(long[] list){
        int n = list.length;
        for(int i = 0; i < n-1; i++)
            for(int j = 0; j < n-i-1; j++)
                if(list[j] > list[j+1]){
                    long t = list[j];
                    list[j] = list[j+1];
                    list[j+1] = t;
                }
    }

    public static void insertionSort(long list[]){
        long n = list.length;
        for(int i=1; i<n;i++){
            long key = list[i];
            int j = i-1;

            while(j>=0 && list[j] > key){
                list[j+1] = list[j];
                j = j-1;
            }
            list[j+1] = key;
        }
    }


    public static void mergeSort(long[] a)
    {
        aux = new long[a.length]; // Allocate space just once.
        mergeSort(a, 0, a.length - 1);
    }

    private static void mergeSort(long[] a, int lo, int hi)
    { // Sort a[lo..hi].
        if (hi <= lo) return;
        int mid = lo + (hi - lo)/2;
        mergeSort(a, lo, mid); // Sort left half.
        mergeSort(a, mid+1, hi); // Sort right half.
        mergeSort(a, lo, mid, hi); // Merge results (code on page 271).
    }

    /* return index of the searched number if found, or -1 if not found */
    public static void mergeSort(long[] a, int lo, int mid, int hi)
    { // Merge a[lo..mid] with a[mid+1..hi].
        int i = lo, j = mid+1;
        for (int k = lo; k <= hi; k++) // Copy a[lo..hi] to aux[lo..hi].
            aux[k] = a[k];
        for (int k = lo; k <= hi; k++) // Merge back to a[lo..hi].

            if (i > mid) a[k] = aux[j++];
            else if (j > hi ) a[k] = aux[i++];
            else if (aux[j] < aux[i]) a[k] = aux[j++];
            else a[k] = aux[i++];
    }

    public static void quickSort(long[] list){
        quickSortWorker(list, 0, list.length-1);
    }
    public static void quickSortWorker(long[] list, int lo, int hi){
        if(lo<=hi)
            return;
        int a = hi-lo/2;
        int pivot = (int)Math.ceil(a);
        int left = lo+1;
        int right = hi;
        long pivotVal = list[pivot];
        while( left <= right ){
            while(left <= hi && pivotVal >= list[left]){
                left++;
            }
            while(right > lo && pivotVal < list[right]){
                right--;
            }
            if(left < right){
                long t = list[left];
                list[left] = list[right];
                list[right] = t;
            }
        }
        long t = list[pivot];
        list[pivot] = list[left-1];
        list[left-1] = t;
        quickSortWorker(list,lo, right-1);
        quickSortWorker(list,right+1,hi);
    }

    public static void naiveQuickSort(long[] list){
        naiveQuickSortWorker(list, 0, list.length-1);
    }
    public static void naiveQuickSortWorker(long[] list, int lo, int hi){
        if(lo<=hi)
            return;
        int pivot = lo;
        int left = lo+1;
        int right = hi;
        long pivotVal = list[pivot];
        while( left <= right ){
            while(left <= hi && pivotVal >= list[left]){
                left++;
            }
            while(right > lo && pivotVal < list[right]){
                right--;
            }
            if(left < right){
                long t = list[left];
                list[left] = list[right];
                list[right] = t;
            }
        }
        long t = list[pivot];
        list[pivot] = list[left-1];
        list[left-1] = t;
        quickSortWorker(list,lo, right-1);
        quickSortWorker(list,right+1,hi);
    }
}
