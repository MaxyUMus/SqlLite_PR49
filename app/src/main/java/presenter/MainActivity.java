package presenter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codebears.sqllite.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.AppDatabase;
import model.User;
import view.MainView;

public class MainActivity extends AppCompatActivity implements MainView {

    private AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText editTextName, editTextEmail, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        db = AppDatabase.getInstance(this);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            DisplayResult("Please fill all fields");
            return;
        }

        // Проверяем, существует ли пользователь с таким email
        executorService.execute(() -> {
            User existingUser = db.userDao().findByEmail(email);
            if (existingUser == null) {
                // Пользователь не существует, можно зарегистрировать нового
                User user = new User(name, email, password);
                db.userDao().insert(user);
                runOnUiThread(() -> {
                    DisplayResult("User registered successfully");
                });
            } else {
                // Пользователь с таким email уже существует
                runOnUiThread(() -> {
                    DisplayResult( "User already exists");
                });
            }
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        new Thread(() -> {
            User user = db.userDao().login(email, password);
            if (user != null) {
                runOnUiThread(() -> {
                    DisplayResult("Login successfully!");
                });
            } else {
                runOnUiThread(() -> DisplayResult("Invalid email or password"));
            }
        }).start();
    }

    @Override
    public void DisplayResult(String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }
}