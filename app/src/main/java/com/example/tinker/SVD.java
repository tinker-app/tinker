package com.example.tinker;

import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.math3.linear.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SVD {
    private RealMatrix laptop_latent_factors;
    private RealMatrix phone_latent_factors;
    private RealMatrix tablet_latent_factors;
    public SVD(List<Product> laptops, List<Product> phones, List<Product> tablets) {
        RealMatrix A = MatrixUtils.createRealMatrix(DataProcessor.getProductFeatureMatrix(laptops));
        SingularValueDecomposition svd = new SingularValueDecomposition(A);
        laptop_latent_factors = svd.getU();

        A = MatrixUtils.createRealMatrix(DataProcessor.getProductFeatureMatrix(phones));
        svd = new SingularValueDecomposition(A);
        phone_latent_factors = svd.getU();

        A = MatrixUtils.createRealMatrix(DataProcessor.getProductFeatureMatrix(tablets));
        svd = new SingularValueDecomposition(A);
        tablet_latent_factors = svd.getU();
    }

    public RealMatrix getTabletLatentFactors() {
        return tablet_latent_factors;
    }

    public RealMatrix getPhoneLatentFactors() {
        return phone_latent_factors;
    }

    public RealMatrix getLaptopLatentFactors() {
        return laptop_latent_factors;
    }
}
