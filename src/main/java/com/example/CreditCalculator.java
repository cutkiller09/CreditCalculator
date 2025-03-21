package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.util.Rotation;
import org.jfree.chart.ui.RectangleInsets;
import java.awt.Color;
import java.awt.BasicStroke;
import org.jfree.chart.axis.CategoryLabelPositions;

public class CreditCalculator extends JFrame {
    private JTextField dureeField, montantField, tauxField, coutMensuelField, montantInteretsField;
    private JButton calculerButton;
    private JTable tableauAmortissement;

    public CreditCalculator() {
        setTitle("Calculateur de Crédit");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        panel.add(new JLabel("Durée du crédit (mois):"));
        dureeField = new JTextField();
        panel.add(dureeField);

        panel.add(new JLabel("Montant du crédit (€):"));
        montantField = new JTextField();
        panel.add(montantField);

        panel.add(new JLabel("Taux du crédit (%):"));
        tauxField = new JTextField();
        panel.add(tauxField);

        panel.add(new JLabel("Coût mensuel (€):"));
        coutMensuelField = new JTextField();
        coutMensuelField.setEditable(false);
        panel.add(coutMensuelField);

        panel.add(new JLabel("Montant cumulé des intérêts (€):"));
        montantInteretsField = new JTextField();
        montantInteretsField.setEditable(false);
        panel.add(montantInteretsField);

        calculerButton = new JButton("Calculer");
        calculerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculerCredit();
            }  
        });
        panel.add(calculerButton);

        // Add amortization table
        String[] columnNames = {"Mensualité", "Capital restant dû", "Amortissement mensuel", "Remboursement mensuel", "Intérêts cumulés", "Intérêts mensuels"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        tableauAmortissement = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tableauAmortissement);
        panel.add(scrollPane);

        add(panel);
    }

    private void calculerCredit() {
        try {
            // Validate inputs
            if (dureeField.getText().isEmpty() || montantField.getText().isEmpty() || tauxField.getText().isEmpty()) {
                throw new NumberFormatException("Tous les champs sont obligatoires.");
            }

            int duree = Integer.parseInt(dureeField.getText());
            double montant = Double.parseDouble(montantField.getText());
            double taux = Double.parseDouble(tauxField.getText());

            if (duree <= 0 || montant <= 0 || taux <= 0) {
                throw new NumberFormatException("Les valeurs doivent être positives.");
            }

            Amortissement amortissement = new Amortissement(duree, montant, taux);
            DefaultTableModel model = amortissement.calculerTableauAmortissement();

            // Set the results in the fields
            double coutMensuel = montant * (taux / 100 / 12) / (1 - Math.pow(1 + (taux / 100 / 12), -duree));
            double montantInterets = (coutMensuel * duree) - montant;

            coutMensuelField.setText(String.format("%.2f", coutMensuel).replace(',', '.'));
            montantInteretsField.setText(String.format("%.2f", montantInterets).replace(',', '.'));

            // Update the amortization table
            tableauAmortissement.setModel(model);

            // Save amortization table to file
            sauvegarderTableauAmortissement(model);

            // Generate charts and display them
            genererCourbes(model);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des valeurs valides. " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sauvegarderTableauAmortissement(DefaultTableModel model) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Amortissement.txt"))) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.write(model.getColumnName(i) + "\\t");
            }
            writer.newLine();

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    writer.write(model.getValueAt(i, j).toString().replace(',', '.') + "\\t");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde du fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void genererCourbes(DefaultTableModel model) {
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();

        for (int i = 0; i < model.getRowCount(); i++) {
            int mensualite = Integer.parseInt(model.getValueAt(i, 0).toString());
            double capitalRestantDu = Double.parseDouble(model.getValueAt(i, 1).toString().replace(',', '.'));
            double amortissementMensuel = Double.parseDouble(model.getValueAt(i, 2).toString().replace(',', '.'));
            double remboursementMensuel = Double.parseDouble(model.getValueAt(i, 3).toString().replace(',', '.'));
            double interetsCumul = Double.parseDouble(model.getValueAt(i, 4).toString().replace(',', '.'));
            double interetsMensuels = Double.parseDouble(model.getValueAt(i, 5).toString().replace(',', '.'));

            // Afficher une valeur sur quatre
            if (i % 4 == 0) {
                String annee = String.format("%.1f", mensualite / 12.0);
                dataset1.addValue(capitalRestantDu, "Capital restant dû", annee);
                dataset1.addValue(interetsCumul, "Intérêts cumulés", annee);
                dataset2.addValue(amortissementMensuel, "Amortissement mensuel", annee);
                dataset2.addValue(remboursementMensuel, "Remboursement mensuel", annee);
                dataset2.addValue(interetsMensuels, "Intérêts mensuels", annee);
            }
        }

        // Get values from input fields
        int duree = Integer.parseInt(dureeField.getText());
        int montant = Integer.parseInt(montantField.getText());
        int taux = Integer.parseInt(tauxField.getText());

        // Create chart title
        String chartTitle = String.format("Amortissement de %d € sur %d mois à %d%%", montant, duree, taux);

        JFreeChart chart = ChartFactory.createLineChart(
                chartTitle,
                "Année",
                "Montant (€)",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setDataset(1, dataset2);
        plot.mapDatasetToRangeAxis(1, 1);

        NumberAxis rangeAxis2 = new NumberAxis("Amortissement, Remboursement, Intérêts mensuels");
        plot.setRangeAxis(1, rangeAxis2);

        LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
        renderer2.setSeriesStroke(0, new BasicStroke(1.0f));
        renderer2.setSeriesStroke(1, new BasicStroke(1.0f));
        renderer2.setSeriesStroke(2, new BasicStroke(1.0f));
        renderer2.setSeriesShapesVisible(0, false); // Amortissement mensuel
        renderer2.setSeriesShapesVisible(1, false); // Remboursement mensuel
        renderer2.setSeriesShapesVisible(2, false); // Intérêts mensuels
        plot.setRenderer(1, renderer2);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        // Set renderer for interests as bar chart
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setSeriesPaint(0, new Color(102, 205, 170));
        barRenderer.setSeriesPaint(1, new Color(255, 182, 193));
        plot.setRenderer(0, barRenderer);

        // Set plot background color
        plot.setBackgroundPaint(new Color(255, 228, 196));
        plot.setOutlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinePaint(Color.GRAY);

        // Set X-axis label rotation and reduce the number of labels
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.getDomainAxis().setMaximumCategoryLabelWidthRatio(0.5f);

        // Display the chart in a window
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame chartFrame = new JFrame("Graphique d'Amortissement");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.add(chartPanel);
        chartFrame.pack();
        chartFrame.setVisible(true);

        try {
            ChartUtils.saveChartAsPNG(new java.io.File("Amortissement.png"), chart, 800, 600);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde de l'image.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        // Create pie chart for credit amount and cumulative interests
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        pieDataset.setValue(String.format("Montant du crédit (€): %.2f", (double) montant), montant);
        pieDataset.setValue(String.format("Intérêts cumulés (€): %.2f", Double.parseDouble(montantInteretsField.getText().replace(',', '.'))), Double.parseDouble(montantInteretsField.getText().replace(',', '.')));

        JFreeChart pieChart = ChartFactory.createPieChart3D(
                "Répartition du crédit",
                pieDataset,
                true,
                true,
                false
        );

        PiePlot3D plot3D = (PiePlot3D) pieChart.getPlot();
        plot3D.setStartAngle(290);
        plot3D.setDirection(Rotation.CLOCKWISE);
        plot3D.setForegroundAlpha(0.5f);
        plot3D.setBackgroundPaint(new Color(255, 228, 196));
        plot3D.setOutlinePaint(Color.BLACK);

        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        JFrame pieChartFrame = new JFrame("Graphique en Camembert 3D");
        pieChartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pieChartFrame.add(pieChartPanel);
        pieChartFrame.pack();
        pieChartFrame.setVisible(true);

        try {
            ChartUtils.saveChartAsPNG(new java.io.File("RepartitionCredit.png"), pieChart, 800, 600);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde de l'image.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CreditCalculator().setVisible(true);
            }
        });
    }
}

class Amortissement {
    private int duree;
    private double montant;
    private double taux;

    public Amortissement(int duree, double montant, double taux) {
        this.duree = duree;
        this.montant = montant;
        this.taux = taux / 100;
    }

    public DefaultTableModel calculerTableauAmortissement() {
        String[] columnNames = {"Mensualité", "Capital restant dû", "Amortissement mensuel", "Remboursement mensuel", "Intérêts cumulés", "Intérêts mensuels"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        double tauxMensuel = taux / 12;
        double coutMensuel = montant * tauxMensuel / (1 - Math.pow(1 + tauxMensuel, -duree));
        double capitalRestant = montant;
        double interetsCumul = 0;

        for (int i = 1; i <= duree; i++) {
            double interetsMensuels = capitalRestant * tauxMensuel;
            double amortissementMensuel = coutMensuel - interetsMensuels;
            capitalRestant -= amortissementMensuel;
            interetsCumul += interetsMensuels;

            Object[] row = {i, String.format("%.2f", capitalRestant).replace(',', '.'), String.format("%.2f", amortissementMensuel).replace(',', '.'), String.format("%.2f", coutMensuel).replace(',', '.'), String.format("%.2f", interetsCumul).replace(',', '.'), String.format("%.2f", interetsMensuels).replace(',', '.')};
            model.addRow(row);
        }

        return model;
    }
}