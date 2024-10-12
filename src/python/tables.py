def create_summary_table(df, instance):
    df_subset = df[df['instance'] == instance] 
    summary_stats = {}

    variable_labels = {
        'time_taken': 'Execution Time (ns)',
        # 'total_cost': 'Total Cost',
        # 'total_distance': 'Total Distance',
        'f_val': 'Objective Function Value'
    }

    # Iterate over each variable to create separate summaries
    for variable, label in variable_labels.items():
        grouped = df_subset.groupby(['method'])[variable].agg(['min', 'max', 'mean']).reset_index()
        grouped.columns = ['method', 'min', 'max', 'mean']

        summary_stats[variable] = grouped

    return variable_labels, summary_stats


def print_solutions_with_highest_fval(doc, df, instance):
    df_subset = df[df['instance'] == instance]
    grouped = df_subset.groupby('method') # by method
    
    for method, group in grouped:
        # best_sol -> highest f_val
        max_fval_row = group.loc[group['f_val'].idxmax()]
        
        doc.add_heading(f"Method: {method}", level=4)
        doc.add_paragraph(f"Highest Objective Function Value (f_val): {max_fval_row['f_val']}")
        
        doc.add_paragraph(f"Solution:")
        solution_paragraph = doc.add_paragraph()
        solution_paragraph.add_run(", ".join(map(str, max_fval_row['solution'])))
        
        doc.add_paragraph("-" * 40)
