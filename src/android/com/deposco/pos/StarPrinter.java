package com.deposco.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.*;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.TextView;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by jaylee on 9/26/14.
 */
public class StarPrinter extends CordovaPlugin {

    private static final String TAG = "StarPrinter";
    public StarPrinter() {}

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	/*
    	 * Don't run any of these if the current activity is finishing
    	 * in order to avoid android.view.WindowManager$BadTokenException
    	 * crashing the app. Just return true here since false should only
    	 * be returned in the event of an invalid action.
    	 */
        if(this.cordova.getActivity().isFinishing()) return true;

        LOG.d(TAG, "ActionName = " + action);
        Context context=this.cordova.getActivity().getApplicationContext();

        List<SalesItem> salesItems = new ArrayList<SalesItem>();

        salesItems.add(new SalesItem("300678566","DEVIATION - POLBKIRI - 4061-13",123.99, 12.3));
        salesItems.add(new SalesItem("300692003","TWENTYSIX.2 - BLUBKIRI - 9177-16",219.99, 21.9));
        salesItems.add(new SalesItem("300651148","GG1045 - 53BLACK - 0ACZ",73.99, 7.3));
        salesItems.add(new SalesItem("300642980","YARDDOG II - 55POLFLI - 355",32900.99, 3290.0));
        salesItems.add(new SalesItem("300638471","EA2004 - GUNMT - 302487",12.99,0));

        String url = "TCP:10.1.1.107";
        if("print".equals(action)) {
            printReceipt(context, cordova.getActivity().getResources(), url, "", salesItems);
        }
        else if("openCashDrawer".equals(action)) {
            openCashDrawer(context, url,"");
        }
/*
        else if("searchTCPPrinters".equals(action)) {
            searchPrinter(context, "TCP", callbackContext);
        }
        else if("searchBTPrinters".equals(action)) {
            searchPrinter(context, "BT", callbackContext);
        }
*/
        //this.alert(args.getString(0), args.getString(1), args.getString(2), callbackContext);
        return true;
    }

    private void searchPrinter(final Context context, final String protocol, final CallbackContext callbackContext) {


        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                List<PortInfo> tcpPortList = null;
                try {
                    tcpPortList = StarIOPort.searchPrinter(protocol+":");
                }
                catch (Throwable e) {
                    LOG.e(TAG,e.getMessage(), e);
                    return;
                }

                if(tcpPortList != null && !tcpPortList.isEmpty()) {
                    List<PortInfo> discoveredPorts = new ArrayList<PortInfo>();
                    for (PortInfo portInfo : tcpPortList) {
                        //Exclude SAC10 model ( no idea why ... )
                        if(!portInfo.getModelName().startsWith("SAC")) {
                            discoveredPorts.add(portInfo);
                        }
                    }

                    List<String> arrayPortName = new ArrayList<String>();

                    for(PortInfo portInfo : discoveredPorts)
                    {
                        String portName;

                        portName = portInfo.getPortName();

                        if(!portInfo.getMacAddress().equals(""))
                        {
                            portName += "\n - " + portInfo.getMacAddress();
                            if(!portInfo.getModelName().equals(""))
                            {
                                portName += "\n - " + portInfo.getModelName();
                            }
                        }

                        arrayPortName.add(portName);

                    }

                    for (String portName : arrayPortName) {
                        LOG.d(TAG,"PortName = " + portName);
                    }
                    /**
                     EditText editPortName = new EditText(cordova.getActivity());

                     new AlertDialog.Builder(cordova.getActivity())
                     .setTitle("Please Select IP Address or Input Port Name")
                     .setCancelable(false)
                     .setView(editPortName)
                     .setPositiveButton("OK", new DialogInterface.OnClickListener()
                     {
                     public void onClick(DialogInterface dialog, int button) {
                     }
                     })
                     .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                     {
                     public void onClick(DialogInterface dialog, int button){
                     }
                     })
                     .setItems(arrayPortName.toArray(new String[0]), new DialogInterface.OnClickListener()
                     {
                     public void onClick(DialogInterface dialog, int select){
                     }
                     })
                     .show();
                     */
                }
            }
        });
    }

    /**
     * This function opens the cash drawer connected to the printer
     * This function just send the byte 0x07 to the printer which is the open cashdrawer command
     * It is not possible that the OpenCashDraware and OpenCashDrawer2 are running at the same time.
     * @param context - Activity for displaying messages to the user
     * @param portName - Port name to use for communication. This should be (TCP:<IPAddress>)
     * @param portSettings - Should be blank
     */
    public void openCashDrawer(final Context context, final String portName, final String portSettings) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                ArrayList<Byte> commands = new ArrayList<Byte>();
                byte openCashDrawer = 0x07;

                commands.add(openCashDrawer);

                sendCommand(context, portName, portSettings, commands);
            }
        });
    }

    private void printReceipt(final Context context, final Resources res,final String portName, final String portSettings, final List<SalesItem> salesItems) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                ArrayList<Byte> list = new ArrayList<Byte>();
                Byte[] tempList;

                int printableArea = 576;    // Printable area in paper is 832(dot)

                RasterDocument rasterDoc = new RasterDocument(RasterDocument.RasSpeed.Medium, RasterDocument.RasPageEndMode.FeedAndFullCut, RasterDocument.RasPageEndMode.FeedAndFullCut, RasterDocument.RasTopMargin.Standard, 0, 0, 0);
                byte[] command = rasterDoc.BeginDocumentCommandData();
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));


                Date date = new Date();
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                DateFormat timeFormat = new SimpleDateFormat("hh:mm a");

                String dateStr = dateFormat.format(date);
                String timeStr = timeFormat.format(date);

                Bitmap bm = BitmapFactory.decodeResource(res, R.drawable.des_logo);
                StarBitmap starbitmap = new StarBitmap(bm, true, 576);
                command = starbitmap.getImageRasterDataForPrinting(false);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                String textToPrint =
                        "                           708 Lincoln Road\r\n" +
                                "                       Miami Beach, FL 33139\r\n\r\n" +
                                "Date: " + dateStr +"                 Time:"+timeStr+"\r\n" +
                                "-----------------------------------------------------------------------\r";
                command = createRasterCommand(textToPrint, printableArea, 13, 0);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                textToPrint = "SALE";
                command = createRasterCommand(textToPrint, printableArea, 13, Typeface.BOLD);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));


                textToPrint = "SKU \t\t                 Description \t\t                Total\r\n";

                command = createRasterCommand(textToPrint, printableArea, 13, 0);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                StringBuilder sb = new StringBuilder();
                double subTotal = 0;
                double tax = 0;
                for (SalesItem salesItem : salesItems) {
                    sb.append(salesItem.getSku()).append("\t\t").append(salesItem.getDescription()).append("\t\t").append(salesItem.getPrice()).append("\n");
                    subTotal += salesItem.getPrice();
                    tax += salesItem.getTax();
                }
                sb.append("\n");
                double total = subTotal + tax;

                textToPrint = sb.toString();
                command = createRasterCommand(textToPrint, printableArea, 10, 0);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                textToPrint =
                        "Subtotal\t\t\t\t                                            "+subTotal+"\r\n" +
                                "Tax	\t\t\t\t                                                "+tax+"\r\n" +
                                "-----------------------------------------------------------------------\r\n" +
                                "Total  \t\t\t\t                                              $"+total+"\r\n" +
                                "-----------------------------------------------------------------------\r\n\r\n" +
                                "Charge\r\n"+total+"\r\n" +
                                "Visa XXXX-XXXX-XXXX-0123\r\n";

                command = createRasterCommand(textToPrint, printableArea, 12, 0);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                textToPrint = ("Refunds and Exchanges");
                command = createRasterCommand(textToPrint, printableArea, 13, Typeface.BOLD);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                textToPrint = ("Within 30 days with receipt\r\n" + "And tags attached");
                command = createRasterCommand(textToPrint, printableArea, 13, 0);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                command = rasterDoc.EndDocumentCommandData();
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));
                list.addAll(Arrays.asList(new Byte[]{0x07}));                // Kick cash drawer

                sendCommand(context, portName, portSettings, list);
            }
        });
    }

    private static byte[] createRasterCommand(String printText, int printableArea, int textSize, int bold) {
        byte[] command;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        Typeface typeface;

        try {
            typeface = Typeface.create(Typeface.SERIF, bold);
        }
        catch (Exception e) {
            typeface = Typeface.create(Typeface.DEFAULT, bold);
        }

        paint.setTypeface(typeface);
        paint.setTextSize(textSize * 2);
        paint.setLinearText(true);

        TextPaint textpaint = new TextPaint(paint);
        textpaint.setLinearText(true);
        android.text.StaticLayout staticLayout =  new StaticLayout(printText, textpaint, printableArea, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        int height = staticLayout.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(staticLayout.getWidth(), height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        c.drawColor(Color.WHITE);
        c.translate(0, 0);
        staticLayout.draw(c);

        StarBitmap starbitmap = new StarBitmap(bitmap, false, printableArea);

        command = starbitmap.getImageRasterDataForPrinting(true);

        return command;
    }

    private class SalesItem {
        private String sku;
        private String description;
        private double price;
        private double tax;

        SalesItem(String sku, String description, double price, double tax) {
            this.sku = sku;
            this.description = description;
            this.price = price;
            this.tax = tax;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getTax() {
            return tax;
        }

        public void setTax(double tax) {
            this.tax = tax;
        }
    }

    private static void sendCommand(Context context, String portName, String portSettings, ArrayList<Byte> byteList) {
        StarIOPort port = null;
        try {
			/*
				using StarIOPort3.1.jar (support USB Port)
				Android OS Version: upper 2.2
			*/
            port = StarIOPort.getPort(portName, portSettings, 10000, context);
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e) { }

			/*
               Using Begin / End Checked Block method
               When sending large amounts of raster data,
               adjust the value in the timeout in the "StarIOPort.getPort"
               in order to prevent "timeout" of the "endCheckedBlock method" while a printing.

               *If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."),
                 need to change value of timeout more longer in "StarIOPort.getPort" method. (e.g.) 10000 -> 30000
			 */
            StarPrinterStatus status = port.beginCheckedBlock();

            if (status.offline)
            {
                throw new StarIOPortException("A printer is offline");
            }

            byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

            port.setEndCheckedBlockTimeoutMillis(30000);//Change the timeout time of endCheckedBlock method.
            status = port.endCheckedBlock();

            if (status.coverOpen){
                throw new StarIOPortException("Printer cover is open");
            }
            else if (status.receiptPaperEmpty){
                throw new StarIOPortException("Receipt paper is empty");
            }
            else if (status.offline){
                throw new StarIOPortException("Printer is offline");
            }
        }
        catch (StarIOPortException e) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("Ok", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage(e.getMessage());
            alert.setCancelable(false);
            alert.show();
        }
        finally
        {
            if (port != null)
            {
                try
                {
                    StarIOPort.releasePort(port);
                }
                catch (StarIOPortException e) { }
            }
        }
    }

    private static void CopyArray(byte[] srcArray, Byte[] cpyArray) {
        for (int index = 0; index < cpyArray.length; index++) {
            cpyArray[index] = srcArray[index];
        }
    }

    private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray) {
        byte[] byteArray = new byte[ByteArray.size()];
        for(int index = 0; index < byteArray.length; index++)
        {
            byteArray[index] = ByteArray.get(index);
        }

        return byteArray;
    }


    /**
     * Builds and shows a native Android alert with given Strings
     * @param message           The message the alert should display
     * @param title             The title of the alert
     * @param buttonLabel       The label of the button
     * @param callbackContext   The callback context
     */
    public synchronized void alert(final String message, final String title, final String buttonLabel, final CallbackContext callbackContext) {
        final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() {
            public void run() {

                AlertDialog.Builder dlg = new AlertDialog.Builder(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                dlg.setMessage(message);
                dlg.setTitle(title);
                dlg.setCancelable(true);
                dlg.setPositiveButton(buttonLabel,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
                            }
                        });
                dlg.setOnCancelListener(new AlertDialog.OnCancelListener() {
                    public void onCancel(DialogInterface dialog)
                    {
                        dialog.dismiss();
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
                    }
                });

                dlg.create();
                AlertDialog dialog =  dlg.show();
                TextView messageview = (TextView)dialog.findViewById(android.R.id.message);
                messageview.setTextDirection(android.view.View.TEXT_DIRECTION_LOCALE);
            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }

}
