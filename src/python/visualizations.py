import pandas as pd
from utils import read_input
import matplotlib.pyplot as plt
from io import BytesIO
from docx.shared import Inches

def generate_visualizations(doc, df, instance):
    problem = read_input(instance)
    df_subset = df[df['instance'] == instance]
    grouped = df_subset.groupby('method') # by method

    for method, group in grouped:
        doc.add_heading(f"Method: {method}", level=4)
        # best_sol -> lowest f_val
        min_fval_row = group.loc[group['f_val'].idxmin()]
        doc.add_paragraph(f"Obj.f. value: {min_fval_row['f_val']}")
        solution_indices = min_fval_row['solution']

        problem_subset = problem.iloc[solution_indices]

        all_x_coords = problem['x'].values
        all_y_coords = problem['y'].values
        subset_x_coords = problem_subset['x'].values
        subset_y_coords = problem_subset['y'].values
        subset_costs = problem_subset['cost'].values  # Get costs for coloring nodes

        # ensure that higher costs correspond to more vibrant colors
        norm = plt.Normalize(subset_costs.min(), subset_costs.max()) 
        node_colors = plt.cm.hot_r(norm(subset_costs))  
        # sizes
        sizes = 100 * (subset_costs - subset_costs.min()) / (subset_costs.max() - subset_costs.min()) + 10  # Size scale


        plt.figure(figsize=(10, 8))
        plt.scatter(all_x_coords, all_y_coords, c='lightgray', s=50, edgecolor='black', alpha=0.5)  # All nodes

        for i in range(len(solution_indices) - 1):
            plt.plot([subset_x_coords[i], subset_x_coords[i + 1]], [subset_y_coords[i], subset_y_coords[i + 1]], 'gray', linewidth=1)
        plt.plot([subset_x_coords[-1], subset_x_coords[0]], [subset_y_coords[-1], subset_y_coords[0]], 'gray', linewidth=1)

        plt.scatter(subset_x_coords, subset_y_coords, c=node_colors, s=sizes, edgecolor='black', alpha=0.8)

        plt.title(f'Visualization for Method: {method}, Instance: {instance}')
        plt.xlabel('X Coordinate')
        plt.ylabel('Y Coordinate')
        plt.grid(True)
        

        for i, (x, y) in enumerate(zip(subset_x_coords, subset_y_coords)):
            plt.annotate(f'{i}', (x, y), textcoords="offset points", xytext=(0, 10), ha='center')


        sm = plt.cm.ScalarMappable(cmap='hot_r', norm=norm)
        sm.set_array([]) 
        cbar = plt.colorbar(sm, ax=plt.gca()) 
        cbar.set_label('Cost', rotation=270, labelpad=15)
 
        # save to doc
        memfile = BytesIO()
        plt.savefig(memfile)
        doc.add_picture(memfile, width=Inches(5))
        doc.add_paragraph()
        memfile.close()
