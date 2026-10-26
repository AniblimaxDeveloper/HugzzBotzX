package com.hugzzbotzx;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.security.SecureRandom;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(9, 11, 19);
    private static final int PANEL = Color.rgb(19, 22, 35);
    private static final int PANEL_2 = Color.rgb(27, 31, 48);
    private static final int PURPLE = Color.rgb(167, 139, 250);
    private static final int MINT = Color.rgb(110, 231, 183);
    private static final int WHITE = Color.rgb(245, 247, 255);
    private static final int MUTED = Color.rgb(157, 165, 187);

    private final SecureRandom random = new SecureRandom();
    private TextView statusText;
    private TextView pairingCodeText;
    private TextView statusPill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        setContentView(createScreen());
    }

    private View createScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(20), dp(20), dp(30));
        scroll.addView(page);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        TextView logo = text("H", 22, WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(round(PURPLE, 16));
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(48), dp(48));
        header.addView(logo, logoLp);

        LinearLayout brandBlock = new LinearLayout(this);
        brandBlock.setOrientation(LinearLayout.VERTICAL);
        brandBlock.setPadding(dp(12), 0, 0, 0);
        brandBlock.addView(text("HUGZZBOTZX", 17, WHITE, true));
        brandBlock.addView(text("REMOTE SUPPORT CONSOLE", 10, MUTED, true));
        header.addView(brandBlock, new LinearLayout.LayoutParams(0, -2, 1));

        TextView demoBadge = text("DEMO", 10, MINT, true);
        demoBadge.setPadding(dp(11), dp(7), dp(11), dp(7));
        demoBadge.setBackground(round(Color.rgb(20, 52, 48), 20));
        header.addView(demoBadge);
        page.addView(header);
        gap(page, 28);

        TextView eyebrow = text("REMOTE SUPPORT  /  SECURE BY CONSENT", 10, PURPLE, true);
        page.addView(eyebrow);
        gap(page, 8);
        page.addView(text("Bantuan jarak jauh.\nTetap di bawah kendalimu.", 29, WHITE, true));
        gap(page, 10);
        page.addView(text("Pasangkan perangkat secara transparan. Pemilik perangkat harus menyetujui sesi dan dapat menghentikannya kapan saja.", 14, MUTED, false));
        gap(page, 22);

        LinearLayout statusCard = card();
        LinearLayout statusTop = new LinearLayout(this);
        statusTop.setGravity(Gravity.CENTER_VERTICAL);
        statusTop.addView(text("STATUS SESI", 11, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1));
        statusPill = text("●  OFFLINE", 10, MUTED, true);
        statusPill.setPadding(dp(10), dp(6), dp(10), dp(6));
        statusPill.setBackground(round(PANEL_2, 20));
        statusTop.addView(statusPill);
        statusCard.addView(statusTop);
        gap(statusCard, 14);
        statusText = text("Belum ada sesi aktif", 19, WHITE, true);
        statusCard.addView(statusText);
        gap(statusCard, 5);
        statusCard.addView(text("Demo UI lokal — belum terkoneksi ke perangkat lain.", 12, MUTED, false));
        page.addView(statusCard);
        gap(page, 16);

        LinearLayout pairCard = card();
        pairCard.addView(text("01  /  DEVICE PAIRING", 11, PURPLE, true));
        gap(pairCard, 10);
        pairCard.addView(text("Kode pemasangan", 18, WHITE, true));
        gap(pairCard, 5);
        pairCard.addView(text("Buat kode contoh untuk alur UI. Kode ini belum dapat dipakai menghubungkan perangkat sungguhan.", 12, MUTED, false));
        gap(pairCard, 14);
        pairingCodeText = text("—  —  —  —  —  —", 24, WHITE, true);
        pairingCodeText.setGravity(Gravity.CENTER);
        pairingCodeText.setLetterSpacing(0.12f);
        pairingCodeText.setPadding(0, dp(14), 0, dp(14));
        pairingCodeText.setBackground(round(PANEL_2, 14));
        pairCard.addView(pairingCodeText, new LinearLayout.LayoutParams(-1, -2));
        gap(pairCard, 12);
        Button generateButton = primaryButton("Buat kode pairing");
        generateButton.setOnClickListener(v -> {
            int code = random.nextInt(900000) + 100000;
            pairingCodeText.setText(String.format(java.util.Locale.US, "%06d", code));
            statusText.setText("Kode demo dibuat di perangkat ini");
            statusPill.setText("●  DEMO");
            statusPill.setTextColor(PURPLE);
        });
        pairCard.addView(generateButton, matchWrap());
        page.addView(pairCard);
        gap(page, 16);

        LinearLayout consentCard = card();
        consentCard.addView(text("02  /  SESSION PERMISSION", 11, PURPLE, true));
        gap(consentCard, 10);
        consentCard.addView(text("Persetujuan pemilik perangkat", 18, WHITE, true));
        gap(consentCard, 5);
        consentCard.addView(text("Sebelum fitur berbagi layar atau kendali diaktifkan pada versi berikutnya, pemilik perangkat harus melihat dan menyetujui permintaan secara jelas.", 12, MUTED, false));
        gap(consentCard, 14);
        Button consentButton = primaryButton("Tinjau persetujuan (simulasi)");
        consentButton.setOnClickListener(v -> showConsentDialog());
        consentCard.addView(consentButton, matchWrap());
        gap(consentCard, 9);
        Button stopButton = secondaryButton("Hentikan / reset sesi demo");
        stopButton.setOnClickListener(v -> {
            pairingCodeText.setText("—  —  —  —  —  —");
            statusText.setText("Sesi demo dihentikan");
            statusPill.setText("●  OFFLINE");
            statusPill.setTextColor(MUTED);
        });
        consentCard.addView(stopButton, matchWrap());
        page.addView(consentCard);
        gap(page, 22);

        page.addView(text("CONTROL CENTER", 11, PURPLE, true));
        gap(page, 10);
        addFeature(page, "▣", "Berbagi layar", "Belum diaktifkan", "Tampilkan layar hanya setelah izin Android diberikan.");
        addFeature(page, "⌁", "Kontrol sentuh", "Belum diaktifkan", "Kontrol interaksi memerlukan izin aksesibilitas yang diaktifkan pemilik perangkat.");
        addFeature(page, "⇄", "Transfer file", "Belum diaktifkan", "Transfer harus dipilih dan dikonfirmasi oleh pengguna.");
        addFeature(page, "◈", "Riwayat sesi", "Lokal saja", "Versi demo tidak mengirim atau menyimpan riwayat ke server.");

        gap(page, 18);
        TextView footer = text("HUGZZBOTZX  •  CONSENT-FIRST  •  BUILD 1.0.0-DEMO", 9, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        page.addView(footer);
        gap(page, 8);
        TextView warning = text("Catatan: ini adalah fondasi UI yang bisa dibuild, bukan aplikasi remote-control produksi. Belum ada streaming layar, koneksi antarperangkat, atau eksekusi perintah jarak jauh.", 11, MUTED, false);
        warning.setGravity(Gravity.CENTER);
        page.addView(warning);

        return scroll;
    }

    private void showConsentDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Persetujuan sesi bantuan")
            .setMessage("SIMULASI LOKAL\n\nVersi demo ini belum mengakses atau mengirim layar, kamera, mikrofon, file, maupun input perangkat. Pada versi lanjutan, sesi nyata hanya boleh dimulai setelah pemilik perangkat menyetujui izin Android dan dapat menghentikannya kapan saja.\n\nLanjutkan untuk menandai persetujuan demo?")
            .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
            .setPositiveButton("Setuju untuk demo", (dialog, which) -> {
                statusText.setText("Persetujuan demo dicatat secara lokal");
                statusPill.setText("●  DEMO OK");
                statusPill.setTextColor(MINT);
            })
            .show();
    }

    private void addFeature(LinearLayout parent, String symbol, String title, String status, String detail) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(14), dp(13), dp(14), dp(13));
        item.setBackground(round(PANEL, 16));

        TextView icon = text(symbol, 19, PURPLE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(round(PANEL_2, 12));
        item.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(12), 0, dp(7), 0);
        details.addView(text(title, 14, WHITE, true));
        gap(details, 3);
        details.addView(text(detail, 11, MUTED, false));
        LinearLayout.LayoutParams detailsLp = new LinearLayout.LayoutParams(0, -2, 1);
        item.addView(details, detailsLp);

        TextView badge = text(status, 9, MUTED, true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(7), dp(6), dp(7), dp(6));
        badge.setBackground(round(PANEL_2, 10));
        item.addView(badge);
        parent.addView(item, matchWrap());
        gap(parent, 8);
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(16), dp(16), dp(16));
        box.setBackground(round(PANEL, 20));
        return box;
    }

    private Button primaryButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.rgb(16, 13, 29));
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setMinHeight(dp(50));
        b.setBackground(round(PURPLE, 14));
        return b;
    }

    private Button secondaryButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(WHITE);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setMinHeight(dp(48));
        b.setBackground(round(PANEL_2, 14));
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setFontFeatureSettings("kern");
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return t;
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radiusDp));
        return bg;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void gap(LinearLayout parent, int heightDp) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(1, dp(heightDp)));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
