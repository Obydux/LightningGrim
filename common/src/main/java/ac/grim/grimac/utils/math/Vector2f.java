package ac.grim.grimac.utils.math;

public class Vector2f {

    public float x;
    public float y;

    public Vector2f() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float length() {
        return GrimMath.sqrt(this.x * this.x + this.y * this.y);
    }

    public Vector2f mul(float v) {
        this.x = this.x * v;
        this.y = this.y * v;
        return this;
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }
}
