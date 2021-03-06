package com.deposco.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.os.Environment;
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

import java.io.*;
import java.net.URL;
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

        salesItems.add(new SalesItem("300678566","DEVIATION - POLBKIRI - 4061-1234567890ABCDEFG",2,123.99, 12.39));
        salesItems.add(new SalesItem("300692003","TWENTYSIX.2 - BLUBKIRI - 9177-16",1,219.99, 21.99));
        salesItems.add(new SalesItem("300651148","GG1045 - 53BLACK - 0ACZ",1,73.99, 7.39));
        salesItems.add(new SalesItem("300642980","YARDDOG II - 55POLFLI - 355",1,32900.99, 3290.00));
        salesItems.add(new SalesItem("300638471","EA2004 - GUNMT - 302487",1,12.99,0));

        List<PaymentItem> paymentItems = new ArrayList<PaymentItem>();
        paymentItems.add(new PaymentItem("VISA", "XXXX-XXXXXX-1234",null,36787.71));

        String printerURL = "TCP:10.1.1.107";
        if("print".equals(action)) {
            String companyCode = args.getString(0);
            String orderNumber = args.getString(1);
            printReceipt(context, companyCode, printerURL, "", orderNumber, salesItems,paymentItems);
        }
        else if("openCashDrawer".equals(action)) {
            openCashDrawer(context, printerURL,"");
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

    private void printReceipt(final Context context, final String companyCode, final String portName, final String portSettings, final String orderNumber, final List<SalesItem> salesItems, final List<PaymentItem> paymentItems) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                ArrayList<Byte> list = new ArrayList<Byte>();
                Byte[] tempList;

                int printableArea = 576;

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

                String imageUrl = "http://images.deposco.com/"+companyCode+"/pos_receipt_logo.png";

                InputStream inputStream= null;
                try {
        /*
                        Bitmap bm = BitmapFactory.decodeResource(res, R.drawable.des_logo);
        */
                    inputStream = (InputStream) new URL(imageUrl).getContent();
                } catch (IOException e) {
                    LOG.e(TAG,"Can't open the receipt logo image file", e);
                }
                if(inputStream != null) {
                    Bitmap bm = BitmapFactory.decodeStream(inputStream);
                    StarBitmap starbitmap = new StarBitmap(bm, true, 576);
                    command = starbitmap.getImageRasterDataForPrinting(false);
                    tempList = new Byte[command.length];
                    CopyArray(command, tempList);
                    list.addAll(Arrays.asList(tempList));
                }

                String address1 = "708 Lincoln Road";
                String city = "Miami Beach";
                String state = "FL";
                String zipCode = "33139";

                StringBuilder sb = new StringBuilder();
                sb.append(address1).append("\n");
                sb.append(city).append(",").append(state).append(" ").append(zipCode).append("\n\n");
                sb.append("Date : ").append(dateStr).append("                 ").append("Time : ").append(timeStr).append("\n");
                sb.append("-----------------------------------------------------------------------------\n");
                command = createRasterCommand(sb.toString(), printableArea, 13, 0,Layout.Alignment.ALIGN_CENTER,false);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                command = createRasterCommand("SALE", printableArea, 13, Typeface.BOLD,Layout.Alignment.ALIGN_NORMAL,false);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                String salesperson = "Larry Johnson";
                String customer = "Jorge Pasada";

                sb = new StringBuilder();
                sb.append("Sales person : ").append(salesperson).append("\n");
                sb.append("Customer     : ").append(customer).append("\n");
                sb.append("Order Number : ").append(orderNumber).append("\n");
                sb.append("--------------------------------------------------------------------------\n");
                command = createRasterCommand(sb.toString(), printableArea, 12, 0,Layout.Alignment.ALIGN_NORMAL,false);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                command = createRasterCommand("Item\t\t\t\t\t\t\t                            QTY\t\t\tPrice\r\n", printableArea, 13, 0,Layout.Alignment.ALIGN_NORMAL,false);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                sb = new StringBuilder();
                double subTotal = 0.0d;
                double tax = 0;

                int maxDescriptionSize = 28;
                int maxQtySize =3;
                int maxPriceSize = 10;
                for (SalesItem salesItem : salesItems) {
                    String [] description = fillSpace(salesItem.getDescription(), maxDescriptionSize, true);
                    double itemTotal = salesItem.getPrice() * ((double)salesItem.getQty());

                    String [] qty = fillSpace(String.valueOf(salesItem.getQty()), maxQtySize, false);
                    String [] totalStr = fillSpace(String.valueOf(itemTotal), maxPriceSize, false);
                    sb.append(description[0]).append("  ").append(qty[0]).append("  ").append(totalStr[0]).append("\n");
                    if(description.length > 1) {
                        sb.append(description[1]).append("\n");
                    }
                    subTotal += itemTotal;
                    tax += salesItem.getTax();
                }
                sb.append("\n");
                subTotal = Math.round( subTotal * 100.0 ) / 100.0;
                double total = subTotal + tax;

                command = createRasterCommand(sb.toString(), printableArea, 10, 0,Layout.Alignment.ALIGN_NORMAL,true);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                double paymentTotal = 0;
                for (PaymentItem paymentItem : paymentItems) {
                    paymentTotal += paymentItem.amount;
                }

                sb = new StringBuilder();

                int maxTotalSize = 30;
                sb.append("Subtotal").append(fillSpace(String.valueOf(subTotal),maxTotalSize, false)[0]).append("\n");
                sb.append("Tax     ").append(fillSpace(String.valueOf(tax),maxTotalSize, false)[0]).append("\n");
                sb.append("---------------------------------------");
                sb.append("Total   ").append(fillSpace(String.valueOf(total),maxTotalSize, false)[0]).append("\n");
                sb.append("Charge  ").append(fillSpace(String.valueOf(paymentTotal),maxTotalSize, false)[0]).append("\n");
                sb.append("---------------------------------------");
                sb.append("Change  ").append(fillSpace(String.valueOf(total - paymentTotal),maxTotalSize, false)[0]).append("\n");
                sb.append("---------------------------------------");

                sb.append("Charge").append("\n");

                for (PaymentItem paymentItem : paymentItems) {
                    sb.append(fillSpace(String.valueOf(paymentItem.type),8, true)[0]);
                    sb.append(fillSpace(String.valueOf(paymentItem.cardNumber),20, true)[0]);
                    sb.append(fillSpace(String.valueOf(paymentItem.amount), 10, false)[0]);
                    sb.append("\n");
                }

                command = createRasterCommand(sb.toString(), printableArea, 12, 0,Layout.Alignment.ALIGN_NORMAL,true);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                command = createRasterCommand("Refunds and Exchanges", printableArea, 13, Typeface.BOLD,Layout.Alignment.ALIGN_NORMAL,false);
                tempList = new Byte[command.length];
                CopyArray(command, tempList);
                list.addAll(Arrays.asList(tempList));

                command = createRasterCommand("Within 30 days with receipt\r\n" + "And tags attached", printableArea, 13, 0,Layout.Alignment.ALIGN_NORMAL,false);
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

    public Bitmap setImageFromByteArray(byte[] byImage) {
        ByteArrayInputStream in = new ByteArrayInputStream(byImage);
        return BitmapFactory.decodeStream(in);
    }

    public byte[] getByteArrayFromImage(Bitmap image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, out);
        return out.toByteArray();
    }

    private void storeImage(Context context, Bitmap image) {
        File pictureFile = getOutputMediaFile(context);
        if (pictureFile == null) {
            LOG.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            LOG.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            LOG.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(Context context){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + context.getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private static String[] fillSpace(String value, int size, boolean fromLeft) {
        if(value == null) {
            value="";
        }
        if(value.length() == size) {
            return new String[] {value};
        }
        else if(value.length() > size){
            return new String[] {value.substring(0,size), value.substring(size)};
        }
        else {
            String space=" ";
            String spaces = "";
            for(int i=0; i< size - value.length();i++) {
                spaces += space;
            }
            return fromLeft ? new String[] {value+spaces} : new String[] {spaces+value};
        }
    }

    private static byte[] createRasterCommand(String printText, int printableArea, int textSize, int bold, Layout.Alignment alignment, boolean isMonoSpaced) {
        byte[] command;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        Typeface typeface;

        Typeface defaultTypeFace = isMonoSpaced ? Typeface.MONOSPACE: Typeface.SANS_SERIF;
        try {
            typeface = Typeface.create(defaultTypeFace, bold);
        }
        catch (Exception e) {
            typeface = Typeface.create(Typeface.DEFAULT, bold);
        }

        paint.setTypeface(typeface);
        paint.setTextSize(textSize * 2);
        paint.setLinearText(true);

        TextPaint textpaint = new TextPaint(paint);
        textpaint.setLinearText(true);
        android.text.StaticLayout staticLayout =  new StaticLayout(printText, textpaint, printableArea, alignment, 1, 0, false);
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
        private int qty;
        private String description;
        private double price;
        private double tax;

        SalesItem(String sku, String description, int qty, double price, double tax) {
            this.sku = sku;
            this.description = description;
            this.qty = qty;
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

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }
    }

    private class PaymentItem {
        private String type;
        private double amount;
        private String cardNumber;
        private String confirmationCode;

        public PaymentItem(String type, String cardNumber, String confirmationCode, double amount) {
            this.type = type;
            this.cardNumber = cardNumber;
            this.confirmationCode = confirmationCode;
            this.amount = amount;
        }
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public String getConfirmationCode() {
            return confirmationCode;
        }

        public void setConfirmationCode(String confirmationCode) {
            this.confirmationCode = confirmationCode;
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
