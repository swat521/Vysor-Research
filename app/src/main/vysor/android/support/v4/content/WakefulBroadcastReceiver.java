// 
// Decompiled by Procyon v0.5.30
// 

package android.support.v4.content;

import android.os.PowerManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.os.PowerManager$WakeLock;
import android.util.SparseArray;
import android.content.BroadcastReceiver;

public abstract class WakefulBroadcastReceiver extends BroadcastReceiver
{
    private static final String EXTRA_WAKE_LOCK_ID = "android.support.content.wakelockid";
    private static final SparseArray<PowerManager$WakeLock> mActiveWakeLocks;
    private static int mNextId;
    
    static {
        mActiveWakeLocks = new SparseArray();
        WakefulBroadcastReceiver.mNextId = 1;
    }
    
    public static boolean completeWakefulIntent(final Intent intent) {
        final int intExtra = intent.getIntExtra("android.support.content.wakelockid", 0);
        boolean b = false;
        if (intExtra != 0) {
            synchronized (WakefulBroadcastReceiver.mActiveWakeLocks) {
                final PowerManager$WakeLock powerManager$WakeLock = (PowerManager$WakeLock)WakefulBroadcastReceiver.mActiveWakeLocks.get(intExtra);
                if (powerManager$WakeLock != null) {
                    powerManager$WakeLock.release();
                    WakefulBroadcastReceiver.mActiveWakeLocks.remove(intExtra);
                    // monitorexit(WakefulBroadcastReceiver.mActiveWakeLocks)
                    b = true;
                }
                else {
                    Log.w("WakefulBroadcastReceiver", "No active wake lock id #" + intExtra);
                    // monitorexit(WakefulBroadcastReceiver.mActiveWakeLocks)
                    b = true;
                }
            }
        }
        return b;
    }
    
    public static ComponentName startWakefulService(final Context context, final Intent intent) {
        ComponentName startService;
        synchronized (WakefulBroadcastReceiver.mActiveWakeLocks) {
            final int mNextId = WakefulBroadcastReceiver.mNextId;
            ++WakefulBroadcastReceiver.mNextId;
            if (WakefulBroadcastReceiver.mNextId <= 0) {
                WakefulBroadcastReceiver.mNextId = 1;
            }
            intent.putExtra("android.support.content.wakelockid", mNextId);
            startService = context.startService(intent);
            if (startService == null) {
                // monitorexit(WakefulBroadcastReceiver.mActiveWakeLocks)
                startService = null;
            }
            else {
                final PowerManager$WakeLock wakeLock = ((PowerManager)context.getSystemService("power")).newWakeLock(1, "wake:" + startService.flattenToShortString());
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire(60000L);
                WakefulBroadcastReceiver.mActiveWakeLocks.put(mNextId, (Object)wakeLock);
            }
        }
        return startService;
    }
}
