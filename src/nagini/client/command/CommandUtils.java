package nagini.client.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * Utility class for Command
 * 
 */
public class CommandUtils {

    /**
     * Utility function that copies a string array except for the first element
     * 
     * @param arr Original array of strings
     * @return Copied array of strings
     */
    public static String[] copyArrayCutFirst(String[] arr) {
        if(arr.length > 1) {
            String[] arrCopy = new String[arr.length - 1];
            System.arraycopy(arr, 1, arrCopy, 0, arrCopy.length);
            return arrCopy;
        } else {
            return new String[0];
        }
    }

    /**
     * Utility function that copies a string array and add another string to
     * first
     * 
     * @param arr Original array of strings
     * @param add
     * @return Copied array of strings
     */
    public static String[] copyArrayAddFirst(String[] arr, String add) {
        String[] arrCopy = new String[arr.length + 1];
        arrCopy[0] = add;
        System.arraycopy(arr, 0, arrCopy, 1, arr.length);
        return arrCopy;
    }

    /**
     * Utility function that pauses and asks for confirmation on dangerous
     * operations.
     * 
     * @param confirm User has already confirmed in command-line input
     * @param opDesc Description of the dangerous operation
     * @throws IOException
     * @return True if user confirms the operation in either command-line input
     *         or here.
     * 
     */
    public static Boolean askConfirm(Boolean confirm, String opDesc) throws IOException {
        if(confirm) {
            System.out.println("Confirmed " + opDesc + " in command-line.");
            return true;
        } else {
            System.out.println("Are you sure you want to " + opDesc + "? (yes/no)");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
            String text = buffer.readLine();
            return text.equals("yes");
        }
    }

    /**
     * Utility function that gives list of values from list of value-pair
     * strings.
     * 
     * @param valueList List of value-pair strings
     * @param delim Delimiter that separates the value pair
     * @returns The list of values; empty if no value-pair is present, The even
     *          elements are the first ones of the value pair, and the odd
     *          elements are the second ones. For example, if the list of
     *          value-pair is ["cluster.xml=file1", "stores.xml=file2"], and the
     *          pair delimiter is '=', we will then have the list of values in
     *          return: ["cluster.xml", "file1", "stores.xml", "file2"].
     */
    public static List<String> getValueList(List<String> valuePairs, String delim) {
        List<String> valueList = Lists.newArrayList();
        for(String valuePair: valuePairs) {
            String[] value = valuePair.split(delim, 2);
            if(value.length != 2)
                throw new RuntimeException("Invalid argument pair: " + value);
            valueList.add(value[0]);
            valueList.add(value[1]);
        }
        return valueList;
    }

    /**
     * Utility function that converts a list to a map.
     * 
     * @param list The list in which even elements are keys and odd elements are
     *        values.
     * @rturn The map container that maps even elements to odd elements, e.g.
     *        0->1, 2->3, etc.
     */
    public static <V> Map<V, V> convertListToMap(List<V> list) {
        Map<V, V> map = new HashMap<V, V>();
        if(list.size() % 2 != 0)
            throw new RuntimeException("Failed to convert list to map.");
        for(int i = 0; i < list.size(); i += 2) {
            map.put(list.get(i), list.get(i + 1));
        }
        return map;
    }

    /**
     * Utility function that creates directory.
     * 
     * @param dir Directory path
     * @return File object of directory.
     */
    public static File createDir(String dir) {
        // create outdir
        File directory = null;
        if(dir != null) {
            directory = new File(dir);
            if(!(directory.exists() || directory.mkdir())) {
                throw new RuntimeException("Can't find or create directory " + dir);
            }
        }
        return directory;
    }
}
