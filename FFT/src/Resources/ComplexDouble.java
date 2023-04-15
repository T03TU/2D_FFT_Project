/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Resources;

/**
 *
 * @author toetu
 */
public class ComplexDouble extends Complex {
    //real - In-phase or Real component
    //Imag  - Phase-quadrature or Imaginary component
    private double real, imag;

    public ComplexDouble() {
        this.real = 0;
        this.imag = 0;
    }

    public ComplexDouble(double re, double im) {
        this.real = re;
        this.imag = im;
    }

    public ComplexDouble(Complex complex) {
        this.real = complex.getReal();
        this.imag = complex.getImaginary();
    }

    @Override
    public Complex add(Complex other) {
        return new ComplexDouble(this.real + other.getReal(), this.imag + other.getImaginary());
    }

    @Override
    public Complex addInPlace(Complex other) {
        this.real += other.getReal();
        this.imag += other.getImaginary();

        return this;
    }

    @Override
    public Complex subtract(Complex other) {
        return new ComplexDouble(this.real - other.getReal(), this.imag - other.getImaginary());
    }

    @Override
    public Complex subtractInPlace(Complex other) {
        this.real -= other.getReal();
        this.imag -= other.getImaginary();
        return this;
    }

    @Override
    public Complex multiply(Complex other) {
                return new ComplexDouble(this.real * other.getReal() - this.imag * other.getImaginary(),
                        this.real * other.getImaginary() + this.imag * other.getReal());
    }

    @Override
    public Complex multiply(double scalar) {
        return new ComplexDouble(this.real * scalar, this.imag * scalar);
    }

    @Override
    public void multiplyInPlace(Complex other) {
        double newReal = this.real * other.getReal() - this.imag * other.getImaginary();
        double newImag = this.real * other.getImaginary() + this.imag * other.getReal();
        this.real = newReal;
        this.imag = newImag;
    }

    @Override
    public Complex multiplyInPlace(double scalar) {
        this.real *= scalar;
        this.imag *= scalar;

        return this;
    }

    @Override
    public Complex divide(Complex other){
        Complex num = this.multiply(other.getConjugate(other));
        Complex den = other.multiply(other.getConjugate(other));
        
        return new ComplexDouble(num.getReal()/den.getReal(), num.getImaginary()/den.getReal());
    }
    
    @Override
    public double getReal() {
        return this.real;
    }

    @Override
    public double getImaginary() {
        return this.imag;
    }

    @Override
    public Complex getUnit() {
        return new ComplexDouble(1, 0);
    }

    @Override
    public Complex getRootOfUnity(double order) {
        double angle = Math.PI * 2.0 / order;
        return new ComplexDouble(Math.cos(angle), -Math.sin(angle));
    }

    @Override
    public Complex getCopy() {
        return new ComplexDouble(this.real, this.imag);
    }

    @Override
    public Complex getNew(double real, double imag) {
        return new ComplexDouble(real, imag);
    }

    @Override
    public Complex getConjugate(Complex number) {
         return new ComplexDouble(number.getReal(), -number.getImaginary());
    }
}
