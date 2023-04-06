package com.li;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();;
        int k = sc.nextInt();

        int[] list = new int[n];

        for(int i = 0; i<n; i++){
            list[i] = sc.nextInt();
        }

        int[][] dp = new int[n+1][k+1];

        for(int i = 1; i<=n; i++){
            for(int j = 0; j<=k; j++){
                dp[i][j] = Math.max(dp[i][j], dp[i-1][j]);
            }
        }

        System.out.println(dp[n][k]);
    }
}
