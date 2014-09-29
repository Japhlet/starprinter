package com.deposco.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaylee on 9/26/14.
 */
public class StarPrinter extends CordovaPlugin {
    public StarPrinter() {}

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	/*
    	 * Don't run any of these if the current activity is finishing
    	 * in order to avoid android.view.WindowManager$BadTokenException
    	 * crashing the app. Just return true here since false should only
    	 * be returned in the event of an invalid action.
    	 */
        if(this.cordova.getActivity().isFinishing()) return true;

        LOG.d("StarPrinter", "ActionName = " + action);
        Context context=this.cordova.getActivity().getApplicationContext();
        openCashDrawer(context, "TCP:10.1.1.107", "");
        //this.alert(args.getString(0), args.getString(1), args.getString(2), callbackContext);
         return true;
    }

    /**
     * This function opens the cash drawer connected to the printer
     * This function just send the byte 0x07 to the printer which is the open cashdrawer command
     * It is not possible that the OpenCashDraware and OpenCashDrawer2 are running at the same time.
     * @param context - Activity for displaying messages to the user
     * @param portName - Port name to use for communication. This should be (TCP:<IPAddress>)
     * @param portSettings - Should be blank
     */
    public static void openCashDrawer(Context context, String portName, String portSettings)
    {
        ArrayList<Byte> commands = new ArrayList<Byte>();
        byte openCashDrawer = 0x07;

        commands.add(openCashDrawer);

        sendCommand(context, portName, portSettings, commands);
    }

    private static void sendCommand(Context context, String portName, String portSettings, ArrayList<Byte> byteList) {
        StarIOPort port = null;
        try
        {
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

            if (true == status.offline)
            {
                throw new StarIOPortException("A printer is offline");
            }

            byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

            port.setEndCheckedBlockTimeoutMillis(30000);//Change the timeout time of endCheckedBlock method.
            status = port.endCheckedBlock();

            if (true == status.coverOpen)
            {
                throw new StarIOPortException("Printer cover is open");
            }
            else if (true == status.receiptPaperEmpty)
            {
                throw new StarIOPortException("Receipt paper is empty");
            }
            else if (true == status.offline)
            {
                throw new StarIOPortException("Printer is offline");
            }
        }
        catch (StarIOPortException e)
        {
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

    private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray)
    {
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
            };
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }

}
