package fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FragmentAddProduct : Fragment() {

    private lateinit var etDepartment: EditText
    private lateinit var ivProductImage: ImageView
    private lateinit var btnUploadImage: Button
    private lateinit var etName: EditText
    private lateinit var etPrice: EditText
    private lateinit var etQuantity: EditText
    private lateinit var btnSubmit: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                ivProductImage.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        etDepartment = view.findViewById(R.id.etDepartment)
        ivProductImage = view.findViewById(R.id.ivProductImage)
        btnUploadImage = view.findViewById(R.id.btnUploadImage)
        etName = view.findViewById(R.id.etName)
        etPrice = view.findViewById(R.id.etPrice)
        etQuantity = view.findViewById(R.id.etQuantity)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Check user role
        checkUserRole()
    }

    private fun checkUserRole() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "user"
                if (role != "admin") {
                    // Redirect to HomePageFragment if not admin
                    (activity as? MainActivity)?.loadFragment(HomePageFragment())
                } else {
                    // If admin, set up the form
                    setupForm()
                }
            }
            .addOnFailureListener {
                // Redirect to HomePageFragment on failure
                (activity as? MainActivity)?.loadFragment(HomePageFragment())
            }
    }

    private fun setupForm() {
        btnUploadImage.setOnClickListener {
            openImagePicker()
        }

        btnSubmit.setOnClickListener {
            val department = etDepartment.text.toString().trim()
            val name = etName.text.toString().trim()
            val price = etPrice.text.toString().trim().toDoubleOrNull() ?: 0.0
            val quantity = etQuantity.text.toString().trim().toIntOrNull() ?: 0

            if (department.isEmpty() || name.isEmpty() || price <= 0 || quantity <= 0 || imageUri == null) {
                Toast.makeText(requireContext(), "Please fill all fields and upload an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Upload image to Firebase Storage
            uploadImageToFirebase { imageUrl ->
                // Save product details to Firestore
                val product = hashMapOf(
                    "department" to department,
                    "imageName" to imageUrl,
                    "name" to name,
                    "price" to price,
                    "quantity" to quantity,
                    "timesSold" to 0
                )

                // Debug: Log the product data
                println("Product data to be added: $product")

                db.collection("Items")
                    .add(product)
                    .addOnSuccessListener { documentReference ->
                        // Debug: Log success
                        println("Product added with ID: ${documentReference.id}")
                        Toast.makeText(requireContext(), "Product added successfully", Toast.LENGTH_SHORT).show()
                        clearForm()
                    }
                    .addOnFailureListener { e ->
                        // Debug: Log failure
                        println("Error adding product: ${e.message}")
                        Toast.makeText(requireContext(), "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(onSuccess: (String) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("item_images/${UUID.randomUUID()}.jpg")

        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    // Get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun clearForm() {
        etDepartment.text.clear()
        ivProductImage.setImageResource(R.color.bg_color)
        etName.text.clear()
        etPrice.text.clear()
        etQuantity.text.clear()
        imageUri = null
    }
}