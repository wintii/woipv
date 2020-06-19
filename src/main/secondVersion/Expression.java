package main.secondVersion;

import java.util.Arrays;

public class Expression {
    public int[] coeffs;
    public int value;
    public ExpressionComparison comparison;

    public Expression() {
        this(3, ExpressionComparison.EQUAL);
    }
    public Expression(int size) {
        this(size, ExpressionComparison.EQUAL);
    }

    public Expression(int size, ExpressionComparison comparison) {
        this.coeffs = new int[size];
        this.comparison = comparison;
    }

    public Expression(int[] coeffs, int value) {
        this.coeffs = new int[coeffs.length];
        for (int i = 0; i < coeffs.length; i++) {
            this.coeffs[i] = -coeffs[i];
        }
        this.value = -value;
    }

    public int getCoeff(int position) {
        return coeffs[position];
    }

    public void setCoeff(int position, int coeff) {
        coeffs[position] = coeff;
    }

    // https://www.geeksforgeeks.org/gcd-two-array-numbers/
    public void doConstraintNormalization() {
        int result = coeffs[0];
        for (int i = 1; i < coeffs.length; i++) {
            result = gcd(coeffs[i], result);

            if (result == 1) {
                return;
            }
        }

        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] /= result;
        }
        value /= result;
    }

    private int gcd(int a, int b) {
        if (a == 0)
            return b;
        return gcd(b % a, a);
    }

    public Expression turnExpression() {
        return new Expression(coeffs, value);
    }

    public Expression addExpression(int i) {
        Expression exp = new Expression(coeffs, value);
        exp.setCoeff(i, 0);
        return exp;
    }

    public void addExpression(int i, Expression b_reason_constraint, int j) {
        for (int index = 0; index < coeffs.length; index++) {
            this.coeffs[index] = i * coeffs[index] + j * b_reason_constraint.getCoeff(index);
        }
        this.value = i * this.value + j * b_reason_constraint.value;
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "coeffsArr=" + Arrays.toString(coeffs) +
                ", bound=" + value +
                ", comparison=" + comparison +
                '}';
    }

    public boolean isEqual(Expression ex) {
        if (value != ex.value)
            return false;
        for (int i = 0; i < coeffs.length; i++)
            if (getCoeff(i) != ex.getCoeff(i))
                return false;

        return true;
    }

    public enum ExpressionComparison {
        SMALLER_EQUAL_THAN,
        BIGGER_EQUAL_THAN,
        EQUAL
    }
}