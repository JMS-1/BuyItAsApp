package de.jochen_manns.buyitv0;

import android.app.Activity;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CategoryList extends Activity {
    private String[] m_categories = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category_list);

        // Der Abruf der Daten für den Vorgang erfolgt asynchron
        new Thread(() -> runOnUiThread(() -> {
            try (Database database = Database.create(this)) {
                m_categories = Products.queryCategories(database);
            } catch (Exception e) {
                // Alle Fehler werden ignoriert
            }
        })).start();
    }
}