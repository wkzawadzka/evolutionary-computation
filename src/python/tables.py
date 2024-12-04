import numpy as np
def create_summary_table(df, instance):
    df_subset = df[df['instance'] == instance] 
    summary_stats = {}

    variable_labels = {
        'time_taken': 'Execution Time (ms)',
        # 'total_cost': 'Total Cost',
        # 'total_distance': 'Total Distance',
        'f_val': 'Objective Function Value',
        'count': 'Iteration Count'
    }

    # Iterate over each variable to create separate summaries
    for variable, label in variable_labels.items():
        if variable not in df_subset.columns:
            continue

        grouped_data = df_subset.groupby(['method'])[variable]
        if grouped_data.size().empty: 
            continue
        grouped = grouped_data.agg(['min', 'max', 'mean']).reset_index()
        grouped.columns = ['method', 'min', 'max', 'mean']

        # Sort by the 'min' value
        grouped = grouped.sort_values(by='min').reset_index(drop=True)

        summary_stats[variable] = grouped

    return variable_labels, summary_stats

def create_intergative_summary_table(df):
    variable_labels = {
        'time_taken': 'Execution Time (ms)',
        # 'total_cost': 'Total Cost',
        # 'total_distance': 'Total Distance',
        'f_val': 'Objective Function Value',
        'count': 'Iteration Count'
    }

    summary_stats = {}

    for variable, label in variable_labels.items():
        if variable not in df.columns:
            continue
        grouped_data = df.groupby(['method', 'instance'])[variable]
        if grouped_data.size().empty: 
            continue
        grouped = grouped_data.agg(['min', 'max', 'mean']).reset_index()
        grouped.columns = ['method', 'instance', 'min', 'max', 'mean']
        grouped = grouped.sort_values(by='min').reset_index(drop=True)
        grouped['val'] = grouped['mean'].astype(str) + ' (' + grouped['min'].astype(str) + ' - ' + grouped['max'].astype(str) + ')'

        # methods as rows and instances as columns
        pivot_table = grouped.pivot(index='method', columns='instance', values='val')
        pivot_table = pivot_table.replace(to_replace=r'nan \(nan - nan\)', value=np.nan, regex=True)
        pivot_table = pivot_table.dropna(how='any')
        if variable == "count":
            print(pivot_table)

        summary_stats[variable] = pivot_table


    return variable_labels, summary_stats

def print_solutions_with_lowest_fval(doc, df, instance):
    df_subset = df[df['instance'] == instance]
    grouped = df_subset.groupby('method') # by method
    
    for method, group in grouped:
        # best_sol -> lowest f_val
        min_fval_row = group.loc[group['f_val'].idxmin()]
        
        doc.add_heading(f"Method: {method}", level=4)
        doc.add_paragraph(f"Lowest Objective Function Value (f_val): {min_fval_row['f_val']}")
        
        doc.add_paragraph(f"Solution:")
        solution_paragraph = doc.add_paragraph()
        solution_paragraph.add_run(", ".join(map(str, min_fval_row['solution'])))
        
        doc.add_paragraph("-" * 40)
