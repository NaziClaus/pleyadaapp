package com.example.pleyadaapp.sftp;

import com.jcraft.jsch.SftpProgressMonitor;

public class ConsoleProgressMonitor implements SftpProgressMonitor {
    private long max = 0;
    private long count = 0;
    private long percent = -1;

    @Override
    public void init(int op, String src, String dest, long max) {
        this.max = max;
        this.count = 0;
        this.percent = -1;
        System.out.printf("Transferring %s to %s (%d bytes)\n", src, dest, max);
    }

    @Override
    public boolean count(long bytes) {
        count += bytes;
        long newPercent = count * 100 / max;
        if (newPercent != percent) {
            percent = newPercent;
            StringBuilder bar = new StringBuilder("[");
            int ticks = (int) (percent / 2); // 50 chars width
            for (int i = 0; i < 50; i++) {
                bar.append(i < ticks ? '=' : ' ');
            }
            bar.append(']');
            System.out.printf("%s %3d%%\r", bar, percent);
        }
        return true;
    }

    @Override
    public void end() {
        System.out.println();
    }
}
